package ru.kyamshanov.mission.gateway.models

class AuthorizationException(
    override val message: String? = null
) : Exception()