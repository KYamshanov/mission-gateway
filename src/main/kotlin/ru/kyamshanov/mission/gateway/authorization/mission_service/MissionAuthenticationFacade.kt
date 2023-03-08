package ru.kyamshanov.mission.gateway.authorization.mission_service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import ru.kyamshanov.mission.gateway.ALLOWING_ROLES_KEY
import ru.kyamshanov.mission.gateway.AUTHORIZATION_DIFFICULTY_KEY
import ru.kyamshanov.mission.gateway.auth_strategy.AccessStrategy
import ru.kyamshanov.mission.gateway.auth_strategy.AuthorizationStrategy
import ru.kyamshanov.mission.gateway.auth_strategy.NoStrategy
import ru.kyamshanov.mission.gateway.authorization.AuthenticationFacade
import ru.kyamshanov.mission.gateway.authorization.AuthenticationService
import ru.kyamshanov.mission.gateway.authorization.AuthenticationServiceQualifier
import ru.kyamshanov.mission.gateway.exception.AuthorizationException
import ru.kyamshanov.mission.gateway.models.AuthorizationDifficulty
import ru.kyamshanov.mission.gateway.models.AuthorizationResult
import ru.kyamshanov.mission.gateway.models.UserInfo

@Component
@AuthenticationServiceQualifier(AuthenticationService.MISSION)
internal class MissionAuthenticationFacade constructor(
    @AuthorizationQualifier(AuthorizationDifficulty.HUGE)
    private val hugeAuthorization: Authorization,
    @AuthorizationQualifier(AuthorizationDifficulty.LIGHT)
    private val lightAuthorization: Authorization,
    private val missionIdentificationFacade: MissionIdentificationFacade,
) : AuthenticationFacade {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun authenticateRequest(exchange: ServerWebExchange): Result<UserInfo?> = coroutineScope {
        runCatching {
            val authorizationDifficulty =
                ((exchange.getAttribute<Route>(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)?.metadata
                    ?.get(AUTHORIZATION_DIFFICULTY_KEY) as? String)
                    ?.let { AuthorizationDifficulty.valueOf(it) } ?: AuthorizationDifficulty.HUGE)

            val authorizationDeferred = async { authorization(authorizationDifficulty, exchange) }
            val identificationDeferred = async { identification(authorizationDifficulty, exchange) }
            val userInfo = identificationDeferred.await()
            compareAuthInfo(authorizationDifficulty, authorizationDeferred.await(), userInfo)
            userInfo
        }
    }


    private suspend fun authorization(
        authorizationDifficulty: AuthorizationDifficulty,
        exchange: ServerWebExchange
    ): AuthorizationResult.Info? {
        val requiredRoles: List<String>? =
            (exchange.getAttribute<Route>(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)?.metadata
                ?.get(ALLOWING_ROLES_KEY) as? LinkedHashMap<*, *>)?.values?.filterIsInstance<String>()

        logger.info("Allowing roles : $requiredRoles")

        val authorizationStrategy = authorizationDifficulty.defineAuthorizationStrategy()

        logger.info("authorization strategy : ${authorizationStrategy::class.simpleName}")

        val authorizationResult =
            requireNotNull(
                authorizationStrategy.authorize(exchange.obtainAccessToken()).getOrThrow()
            ) { "Response of authorize cannot be null" }
                .also { if (it.status != AuthorizationResult.Status.SUCCESS) throw AuthorizationException("Authorize status is ${it.status}") }

        if (requiredRoles != null) {
            val userRoles =
                requireNotNull(authorizationResult.info?.userRoles) { "For auth user roles cannot be null" }
            requiredRoles.find { requiredRole -> userRoles.contains(requiredRole).not() }
                ?.let { throw AuthorizationException("Required $it role") }
        }
        return authorizationResult.info
    }

    private suspend fun identification(
        authorizationDifficulty: AuthorizationDifficulty,
        exchange: ServerWebExchange
    ): UserInfo? {
        if (authorizationDifficulty == AuthorizationDifficulty.NOTHING) return null
        val idToken = requireNotNull(exchange.obtainIdToken()) { "For identification user required idToken" }
        return missionIdentificationFacade.identify(idToken).getOrThrow()
    }

    private fun compareAuthInfo(
        authorizationDifficulty: AuthorizationDifficulty,
        authorizationInfo: AuthorizationResult.Info?,
        userInfo: UserInfo?
    ): Result<Unit> = runCatching {
        if (authorizationDifficulty == AuthorizationDifficulty.NOTHING) return@runCatching
        if (authorizationInfo == null || userInfo == null) throw IllegalArgumentException("Auth info cannot be null")
        if (authorizationInfo.accessId != userInfo.accessId) throw AuthorizationException("Access ids are not equal")
    }

    private fun AuthorizationDifficulty.defineAuthorizationStrategy(): AuthorizationStrategy = when (this) {
        AuthorizationDifficulty.HUGE -> AccessStrategy(hugeAuthorization)
        AuthorizationDifficulty.LIGHT -> AccessStrategy(lightAuthorization)
        AuthorizationDifficulty.NOTHING -> NoStrategy()
    }

    private fun ServerWebExchange.obtainAccessToken(): String? =
        request.headers["Authorization"]?.get(0)

    private fun ServerWebExchange.obtainIdToken(): String? =
        request.headers[IDENTIFICATION_HEADER]?.get(0)

    private companion object {

        const val AUTHORIZATION_HERDER = "Authorization"

        const val IDENTIFICATION_HEADER = "Mission-id"
    }

}