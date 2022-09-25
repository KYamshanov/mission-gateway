package ru.kyamshanov.mission.gateway.filters

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.AUTHORIZATION_DIFFICULTY_KEY
import ru.kyamshanov.mission.gateway.authorization.Authorization
import ru.kyamshanov.mission.gateway.authorization.AuthorizationDifficulty
import ru.kyamshanov.mission.gateway.authorization.AuthorizationQualifier

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

    /**
     * Добавляет запрос в МС авторизации на проверку жизни access токена используя алгоритм авторизации определенный в метадате роута
     *
     * @see [GlobalFilter.filter]
     */
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val authorizationDifficulty = (exchange.getAttribute<Route>(GATEWAY_ROUTE_ATTR)?.metadata
            ?.get(AUTHORIZATION_DIFFICULTY_KEY) as? String)
            ?.let { AuthorizationDifficulty.valueOf(it) } ?: AuthorizationDifficulty.HUGE

        val accessToken = exchange.extractAccessToken()

        val status = when (authorizationDifficulty) {
            AuthorizationDifficulty.NOTHING -> Mono.just(HttpStatus.OK)
            AuthorizationDifficulty.LIGHT -> lightAuthorization.authorizeRequest(accessToken)
            else -> hugeAuthorization.authorizeRequest(accessToken)
        }

        return status.flatMap {
            if (it == HttpStatus.OK) chain.filter(exchange)
            else Mono.error(NullPointerException())
        }
    }

    private fun ServerWebExchange.extractAccessToken(): String =
        request.headers["Authorization"]?.get(0).orEmpty()
}