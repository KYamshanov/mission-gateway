package ru.kyamshanov.mission.gateway.authorization

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
    fun authorizeRequest(accessToken: String): Mono<AuthorizationResponse>
}