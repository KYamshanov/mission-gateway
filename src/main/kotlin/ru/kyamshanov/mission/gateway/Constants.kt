package ru.kyamshanov.mission.gateway

/**
 * Ключ для определения алгоритма авторизации в метахате роута
 */
const val AUTHORIZATION_DIFFICULTY_KEY = "auth_type"

/**
 * Ключ для списка ролей разрешающих доступ
 */
const val ALLOWING_ROLES_KEY = "allowing_roles"

/**
 * Ключ для определения URL запроса авторизации до МС
 */
const val AUTHORIZATION_URI_KEY = "authorization.microservice.url"