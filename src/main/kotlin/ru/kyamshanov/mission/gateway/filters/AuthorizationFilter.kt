package ru.kyamshanov.mission.gateway.filters

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.auth_strategy.AccessStrategy
import ru.kyamshanov.mission.gateway.auth_strategy.AuthorizationStrategy
import ru.kyamshanov.mission.gateway.auth_strategy.NoStrategy
import ru.kyamshanov.mission.gateway.authorization.AuthenticationFactory
import ru.kyamshanov.mission.gateway.authorization.AuthenticationService
import ru.kyamshanov.mission.gateway.authorization.AuthenticationServiceQualifier
import ru.kyamshanov.mission.gateway.models.*

/**
 * Глобальный Фильтр запросов для авторизацации
 */
@Component
internal class AuthorizationFilter(
    private val authenticationFactory: AuthenticationFactory
) : GlobalFilter {

    /**
     * Добавляет запрос в МС авторизации на проверку жизни access токена используя алгоритм авторизации определенный в метадате роута
     *
     * @see [GlobalFilter.filter]
     */
    override fun filter(serverWebExchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> = runBlocking {
        var exchange = serverWebExchange
        val authResult =
            authenticationFactory.createFacade(AuthenticationService.MISSION).authenticateRequest(exchange).getOrThrow()

        if (authResult.externalUserId != null) {
            exchange = exchange.appendHeader(EXTERNAL_ID_HEADER, authResult.externalUserId)
        }
        if (authResult.userId != null) {
            exchange = exchange.appendHeader(USER_ID_HEADER_KEY, authResult.userId)
        }
        if (authResult.accessId != null) {
            exchange = exchange.appendHeader(ACCESS_ID_HEADER, authResult.accessId)
        }

        chain.filter(exchange)
    }

    private fun ServerWebExchange.appendHeader(key: String, value: String) = request.mutate()
        .header(key, value)
        .build().let { mutate().request(it).build() }

    private companion object {
        const val USER_ID_HEADER_KEY = "user-id"
        const val EXTERNAL_ID_HEADER = "external-id"
        const val ACCESS_ID_HEADER = "access-id"
    }
}