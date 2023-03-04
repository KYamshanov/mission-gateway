package ru.kyamshanov.mission.gateway.models

data class AuthorizationResult(
    val status: Status,
    val info: Info?
) {

    data class Info(
        val userRoles: List<String>,
        val externalUserId: String
    )

    //TODO DENIED не используется. Отказ от статуса
    enum class Status {
        SUCCESS,
        DENIED
    }
}