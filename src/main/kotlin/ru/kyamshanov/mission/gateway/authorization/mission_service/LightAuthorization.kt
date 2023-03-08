package ru.kyamshanov.mission.gateway.authorization.mission_service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import ru.kyamshanov.mission.gateway.AUTHORIZATION_URI_KEY
import ru.kyamshanov.mission.gateway.authorization.AuthenticationServiceQualifier
import ru.kyamshanov.mission.gateway.dto.AuthorizationResponse
import ru.kyamshanov.mission.gateway.dto.CheckAccessRqDto
import ru.kyamshanov.mission.gateway.dto.CheckAccessRsDto
import ru.kyamshanov.mission.gateway.models.AuthorizationDifficulty

/**
 * Авторизация использующая только валидацию access токена
 *
 * @property webClient Веб клиент для запросов к МС авторизации
 * @property authorizationUrl URL к МС авторизации для проверки
 */
@Component
@AuthorizationQualifier(AuthorizationDifficulty.LIGHT)
internal class LightAuthorization(
    private val webClient: WebClient,
    @Value("\${$AUTHORIZATION_URI_KEY}")
    private val authorizationUrl: String
) : Authorization {

    override suspend fun authorizeRequest(accessToken: String): Result<AuthorizationResponse> = kotlin.runCatching {
        withContext(Dispatchers.IO) {
            webClient.post().uri(authorizationUrl)
                .bodyValue(CheckAccessRqDto(accessToken, false))
                .awaitExchange { clientResponse ->
                    AuthorizationResponse(
                        clientResponse.statusCode(),
                        clientResponse.awaitBody(CheckAccessRsDto::class)
                    )
                }
        }
    }
}