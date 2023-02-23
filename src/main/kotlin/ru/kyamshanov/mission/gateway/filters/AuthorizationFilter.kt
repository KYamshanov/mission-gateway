package ru.kyamshanov.mission.gateway.filters

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.ALLOWING_ROLES_KEY
import ru.kyamshanov.mission.gateway.AUTHORIZATION_DIFFICULTY_KEY
import ru.kyamshanov.mission.gateway.auth_strategy.AccessStrategy
import ru.kyamshanov.mission.gateway.auth_strategy.AuthorizationStrategy
import ru.kyamshanov.mission.gateway.auth_strategy.NoStrategy
import ru.kyamshanov.mission.gateway.authorization.Authorization
import ru.kyamshanov.mission.gateway.authorization.AuthorizationQualifier
import ru.kyamshanov.mission.gateway.models.*

/**
 * Глобальный Фильтр запросов для авторизацации
 */
@Component
class AuthorizationFilter(
    @AuthorizationQualifier(AuthorizationDifficulty.HUGE)
    private val hugeAuthorization: Authorization,
    @AuthorizationQualifier(AuthorizationDifficulty.LIGHT)
    private val lightAuthorization: Authorization
) : GlobalFilter {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Добавляет запрос в МС авторизации на проверку жизни access токена используя алгоритм авторизации определенный в метадате роута
     *
     * @see [GlobalFilter.filter]
     */
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val allowingRoles: List<String>? = (exchange.getAttribute<Route>(GATEWAY_ROUTE_ATTR)?.metadata
            ?.get(ALLOWING_ROLES_KEY) as? LinkedHashMap<*, *>)?.values?.filterIsInstance<String>()

        logger.info("Allowing roles : $allowingRoles")

        val authorizationStrategy = ((exchange.getAttribute<Route>(GATEWAY_ROUTE_ATTR)?.metadata
            ?.get(AUTHORIZATION_DIFFICULTY_KEY) as? String)
            ?.let { AuthorizationDifficulty.valueOf(it) } ?: AuthorizationDifficulty.HUGE)
            .defineAuthorizationStrategy()

        logger.info("authorization strategy : ${authorizationStrategy::class.simpleName}")

        val monoStatus = authorizationStrategy.authorize(exchange.extractAccessToken())

        return monoStatus.flatMap { status ->
            if (status.status != AuthorizationResult.Status.SUCCESS) Mono.error(AuthorizationException("Authorization status is not SUCCESS"))
            else {
                if (allowingRoles == null) {
                    (status.data?.let { exchange.appendHeaders(status.data.userId) } ?: exchange)
                        .let { chain.filter(it) }
                } else {
                    requireNotNull(status.data) { "For authorize user status.data required" }

                    if (allowingRoles.any { status.data.userRoles.contains(it) }) {
                        chain.filter(exchange.appendHeaders(status.data.userId))
                    } else Mono.error(CredentialsException("User has not need roles. Required any $allowingRoles but user has ${status.data.userRoles}"))
                }
            }
        }.doOnError { logger.error("Auth exception",it) }
    }

    private fun AuthorizationDifficulty.defineAuthorizationStrategy(): AuthorizationStrategy = when (this) {
        AuthorizationDifficulty.HUGE -> AccessStrategy(hugeAuthorization)
        AuthorizationDifficulty.LIGHT -> AccessStrategy(lightAuthorization)
        AuthorizationDifficulty.NOTHING -> NoStrategy()
    }

    private fun ServerWebExchange.extractAccessToken(): String? =
        request.headers["Authorization"]?.get(0)

    private fun ServerWebExchange.appendHeaders(userId: String) = request.mutate()
        .header(USER_ID_HEADER_KEY, userId)
        .build().let { mutate().request(it).build() }

    private companion object {
        const val USER_ID_HEADER_KEY = "user-id"
    }
}