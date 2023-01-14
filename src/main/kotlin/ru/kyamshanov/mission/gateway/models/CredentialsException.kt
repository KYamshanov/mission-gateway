package ru.kyamshanov.mission.gateway.models

import org.springframework.http.HttpStatus

class CredentialsException(
    override val message: String? = null
) : Exception()