package ru.kyamshanov.mission.gateway.authorization

import org.springframework.beans.factory.annotation.Qualifier
import ru.kyamshanov.mission.gateway.models.AuthorizationDifficulty

/**
 * Квалификатор для метода авторизации
 */
@Qualifier
internal annotation class AuthorizationQualifier(

    val difficulty: AuthorizationDifficulty
)