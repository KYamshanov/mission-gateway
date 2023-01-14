package ru.kyamshanov.mission.gateway.filters

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.ALLOWING_ROLES_KEY
import ru.kyamshanov.mission.gateway.AUTHORIZATION_DIFFICULTY_KEY
import ru.kyamshanov.mission.gateway.authorization.Authorization
import ru.kyamshanov.mission.gateway.authorization.AuthorizationQualifier
import ru.kyamshanov.mission.gateway.dto.CheckAccessRsDto
import ru.kyamshanov.mission.gateway.models.AuthorizationDifficulty
import ru.kyamshanov.mission.gateway.models.AuthorizationException
import ru.kyamshanov.mission.gateway.models.AuthorizationStatus
import ru.kyamshanov.mission.gateway.models.CredentialsException

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

    private val logger = LoggerFactory.getLogger(AuthorizationFilter::class.java)

    /**
     * Добавляет запрос в МС авторизации на проверку жизни access токена используя алгоритм авторизации определенный в метадате роута
     *
     * @see [GlobalFilter.filter]
     */
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val allowingRoles: List<String>? = (exchange.getAttribute<Route>(GATEWAY_ROUTE_ATTR)?.metadata
            ?.get(ALLOWING_ROLES_KEY) as? LinkedHashMap<*, *>)?.values?.filterIsInstance<String>()

        logger.debug("Allowing roles $allowingRoles")

        val authorizationDifficulty = (exchange.getAttribute<Route>(GATEWAY_ROUTE_ATTR)?.metadata
            ?.get(AUTHORIZATION_DIFFICULTY_KEY) as? String)
            ?.let { AuthorizationDifficulty.valueOf(it) } ?: AuthorizationDifficulty.HUGE

        logger.debug("Authorization difficulty $authorizationDifficulty")


        val accessToken = exchange.extractAccessToken()

        val monoStatus = when (authorizationDifficulty) {
            AuthorizationDifficulty.NOTHING -> Mono.just(ClientResponse.create(HttpStatus.OK).build())
            AuthorizationDifficulty.LIGHT -> lightAuthorization.authorizeRequest(accessToken)
            else -> hugeAuthorization.authorizeRequest(accessToken)
        }.toStatus()



        return monoStatus.flatMap { status ->

            if (allowingRoles != null && status.userRoles.any { allowingRoles.contains(it) }.not()) {
                Mono.error(CredentialsException())
            } else if (status.httpStatus != HttpStatus.OK) {
                Mono.error(NullPointerException())
            } else chain.filter(exchange.appendHeaders(status.userId))
        }
    }

    private fun ServerWebExchange.extractAccessToken(): String =
        request.headers["Authorization"]?.get(0).orEmpty()

    private fun Mono<ClientResponse>.toStatus(): Mono<AuthorizationStatus> = flatMap {
        if (it.statusCode().is2xxSuccessful) it.toEntity(CheckAccessRsDto::class.java)
        else Mono.error(AuthorizationException(it.statusCode()))
    }.map { responseEntity: ResponseEntity<CheckAccessRsDto> ->
        val roles = responseEntity.body?.accessData?.roles.orEmpty()
        val userId = requireNotNull(responseEntity.body?.accessData?.userId) { "Required external user id" }
        AuthorizationStatus(responseEntity.statusCode, roles, userId)
    }

    private fun ServerWebExchange.appendHeaders(userId: String) = request.mutate()
        .header(USER_ID_HEADER_KEY, userId)
        .build().let { mutate().request(it).build() }

    private companion object {
        const val USER_ID_HEADER_KEY = "user-id"
    }
}