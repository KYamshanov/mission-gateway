package ru.kyamshanov.mission.gateway.authorization

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.AUTHORIZATION_URI_KEY
import ru.kyamshanov.mission.gateway.dto.CheckAccessRqDto

/**
 * Авторизация использующая валидацию и проверку блокировки access токена
 *
 * @property webClient Веб клиент для запросов к МС авторизации
 * @property authorizationUrl URL к МС авторизации для проверки
 */
@Component
@AuthorizationQualifier(AuthorizationDifficulty.HUGE)
internal class HugeAuthorization(
    private val webClient: WebClient,
    @Value("\${$AUTHORIZATION_URI_KEY}")
    private val authorizationUrl: String
) : Authorization {

    override fun authorizeRequest(accessToken: String) =
        webClient.post().uri(authorizationUrl)
            .bodyValue(CheckAccessRqDto(accessToken, true))
            .exchangeToMono { Mono.just(it.statusCode()) }
}