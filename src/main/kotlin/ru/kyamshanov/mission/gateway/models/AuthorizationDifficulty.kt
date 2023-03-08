package ru.kyamshanov.mission.gateway.models
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
    NOTHING,

    /**
     * Метод предоставляющий auth id для конечного end-point
     */
    EXTERNAL_ID_PROVIDER,
}