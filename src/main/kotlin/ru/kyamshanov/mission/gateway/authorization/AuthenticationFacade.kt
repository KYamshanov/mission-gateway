package ru.kyamshanov.mission.gateway.authorization

import org.springframework.web.server.ServerWebExchange
import ru.kyamshanov.mission.gateway.models.UserInfo

internal interface AuthenticationFacade {

    suspend fun authenticateRequest(exchange: ServerWebExchange): Result<UserInfo?>
}