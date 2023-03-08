package ru.kyamshanov.mission.gateway.authorization.mission_service

import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.dto.AuthorizationResponse

/**
 * Интерфейс авторизации
 */
interface Authorization {

    /**
     * Авторизовать запрос по access токену
     * @param accessToken Access токен
     * @return [Mono]<[HttpStatus]> Со статусом ответа от МС авторизации
     *
     * [HttpStatus.OK] - успешная авторизация, любые другие статусы означают авторизация не пройденной
     */
    suspend fun authorizeRequest(accessToken: String): Result<AuthorizationResponse>
}