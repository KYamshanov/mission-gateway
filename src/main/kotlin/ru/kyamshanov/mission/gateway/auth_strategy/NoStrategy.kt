package ru.kyamshanov.mission.gateway.auth_strategy

import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.models.AuthorizationResult

internal class NoStrategy : AuthorizationStrategy {

    override fun authorize(accessToken: String?): Mono<AuthorizationResult> =
        Mono.just(AuthorizationResult(AuthorizationResult.Status.SUCCESS, null))
}