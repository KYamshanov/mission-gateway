package ru.kyamshanov.mission.gateway.dto

import java.io.Serializable

/**
 * Dto-model Access данных
 * @property externalId Внешний ID пользователя
 * @property roles Роли пользователя
 */
data class AccessDataDto(
    val roles: List<String> = emptyList(),
    val externalId: String = "",
    val accessId: String = ""
) : Serializable
