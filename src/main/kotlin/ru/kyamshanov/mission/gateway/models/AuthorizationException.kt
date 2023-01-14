package ru.kyamshanov.mission.gateway.models

import org.springframework.http.HttpStatus

class AuthorizationException(
    val status: HttpStatus,
    override val message: String? = null
) : Exception()