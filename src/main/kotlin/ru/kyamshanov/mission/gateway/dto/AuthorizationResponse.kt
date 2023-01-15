package ru.kyamshanov.mission.gateway.dto

import org.springframework.http.HttpStatus

data class AuthorizationResponse(
    val httpStatus : HttpStatus,
    val checkAccessRsDto : CheckAccessRsDto
)