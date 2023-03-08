package ru.kyamshanov.mission.gateway.auth_strategy

import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.authorization.mission_service.Authorization
import ru.kyamshanov.mission.gateway.dto.CheckAccessRsDto
import ru.kyamshanov.mission.gateway.models.AuthorizationException
import ru.kyamshanov.mission.gateway.models.AuthorizationResult

internal class AccessStrategy(
    private val authorization: Authorization
) : AuthorizationStrategy {

    override suspend fun authorize(accessToken: String?): Result<AuthorizationResult> =
        authorization.authorizeRequest(requireNotNull(accessToken) { "Access token required for authorizeRequest " })
            .mapCatching {
                if (it.httpStatus != HttpStatus.OK) throw AuthorizationException("Status code is ${it.httpStatus.value()} but expected 200")
                else it.checkAccessRsDto
            }
            .mapCatching { status ->
                require(status.status == CheckAccessRsDto.AccessStatus.ACTIVE) { "For ${authorization::class.simpleName} access token status should be ACTIVE. Now it is ${status.status}" }
                requireNotNull(status.accessData) { "For ${authorization::class.simpleName} accessData should not be NULL" }
                val resultStatus = AuthorizationResult.Status.SUCCESS
                val info = AuthorizationResult.Info(
                    status.accessData.roles,
                    status.accessData.externalId,
                    status.accessData.accessId
                )
                AuthorizationResult(resultStatus, info)
            }
}