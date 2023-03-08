package ru.kyamshanov.mission.gateway.dto

data class VerifyRqDto(
    val idToken: String
)

data class VerifyRsDto(
    val internalId: String,
    val accessId: String
)