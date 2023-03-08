package ru.kyamshanov.mission.gateway.authorization.mission_service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import ru.kyamshanov.mission.gateway.dto.*
import ru.kyamshanov.mission.gateway.models.UserInfo

internal interface MissionIdentificationFacade {

    suspend fun identify(idToken: String): Result<UserInfo>
}

@Component
private class MissionIdentificationFacadeImpl @Autowired constructor(
    private val webClient: WebClient,
    @Value("\${ID_VERIFY_URL}")
    private val idVerifyUrl: String
) : MissionIdentificationFacade {

    override suspend fun identify(idToken: String): Result<UserInfo> = runCatching {
        withContext(Dispatchers.IO) {
            webClient.post().uri(idVerifyUrl)
                .bodyValue(VerifyRqDto(idToken))
                .awaitExchange { clientResponse ->
                    clientResponse.awaitBody(VerifyRsDto::class)
                }.toUserInfo()
        }
    }

    private fun VerifyRsDto.toUserInfo() = UserInfo(
        internalId = internalId,
        accessId = accessId
    )
}
