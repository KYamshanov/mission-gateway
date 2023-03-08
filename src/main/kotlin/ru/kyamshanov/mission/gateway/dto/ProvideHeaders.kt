package ru.kyamshanov.mission.gateway.dto

internal data class ProvideHeaders(
    val userId: String? = null,
    val externalUserId: String? = null,
    val accessId: String? = null
)
