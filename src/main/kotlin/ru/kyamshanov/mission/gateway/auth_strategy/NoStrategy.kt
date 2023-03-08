package ru.kyamshanov.mission.gateway.auth_strategy

import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.models.AuthorizationResult

internal class NoStrategy : AuthorizationStrategy {

    override suspend fun authorize(accessToken: String?): Result<AuthorizationResult> = runCatching {
        AuthorizationResult(AuthorizationResult.Status.SUCCESS, null)
    }
}