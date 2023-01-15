package ru.kyamshanov.mission.gateway.models

data class AuthorizationResult(
    val status: Status,
    val data: Data?
) {

    data class Data(
        val userRoles: List<String>,
        val userId: String
    )

    enum class Status {
        SUCCESS,
        DENIED
    }
}