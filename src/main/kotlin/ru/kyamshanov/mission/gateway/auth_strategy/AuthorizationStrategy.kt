package ru.kyamshanov.mission.gateway.auth_strategy

import reactor.core.publisher.Mono
import ru.kyamshanov.mission.gateway.models.AuthorizationResult

interface AuthorizationStrategy {

    suspend fun authorize(accessToken: String?): Result<AuthorizationResult>
}