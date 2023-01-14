package ru.kyamshanov.mission.gateway.models

import org.springframework.http.HttpStatus

data class AuthorizationStatus(
    val httpStatus: HttpStatus,
    val userRoles: List<String>,
    val userId: String
)