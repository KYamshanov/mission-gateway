package ru.kyamshanov.mission.gateway.auth_strategy

import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.authorization.Authorization
import ru.kyamshanov.mission.gateway.dto.CheckAccessRsDto
import ru.kyamshanov.mission.gateway.models.AuthorizationException
import ru.kyamshanov.mission.gateway.models.AuthorizationResult

internal class AccessStrategy(
    private val authorization: Authorization
) : AuthorizationStrategy {

    override fun authorize(accessToken: String?): Mono<AuthorizationResult> =
        authorization.authorizeRequest(requireNotNull(accessToken) { "Access token required for authorizeRequest " })
            .flatMap {
                if (it.httpStatus != HttpStatus.OK) Mono.error(AuthorizationException("Status code is ${it.httpStatus.value()} but expected 200"))
                else Mono.just(it.checkAccessRsDto)
            }
            .map { status ->
                require(status.status == CheckAccessRsDto.AccessStatus.ACTIVE) { "For ${authorization::class.simpleName} access token status should be ACTIVE. Now it is ${status.status}" }
                requireNotNull(status.accessData) { "For ${authorization::class.simpleName} accessData should not be NULL" }
                val resultStatus = AuthorizationResult.Status.SUCCESS
                val data = AuthorizationResult.Data(status.accessData.roles, status.accessData.userId)
                AuthorizationResult(resultStatus, data)
            }
}