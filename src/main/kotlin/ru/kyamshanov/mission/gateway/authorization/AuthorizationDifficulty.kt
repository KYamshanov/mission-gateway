package ru.kyamshanov.mission.gateway.authorization

import org.springframework.beans.factory.annotation.Qualifier

/**
 * Квалификатор для метода авторизации
 */
@Qualifier
internal annotation class AuthorizationQualifier(

    val difficulty: AuthorizationDifficulty
)

/**
 * Список методов авторизации, по сложности
 */
internal enum class AuthorizationDifficulty {

    /**
     * Метод авторизации проверяющий блокировку access токена и его валидность
     */
    HUGE,

    /**
     * Метод авторизации проверяющий валидность access токена
     */
    LIGHT,

    /**
     * Метод авторизации не предусматривающий проверки
     */
    NOTHING
}