package ru.kyamshanov.mission.gateway.dto

/**
 * Dto-model Access данных
 * @property userId Внешний ID пользователя
 * @property roles Роли пользователя
 */
data class AccessDataDto(
    val roles: List<String>,
    val userId: String
)
