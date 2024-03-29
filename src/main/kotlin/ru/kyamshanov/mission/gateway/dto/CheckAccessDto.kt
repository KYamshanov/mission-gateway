package ru.kyamshanov.mission.gateway.dto

/**
 * Dto-model Тело запроса на проверку активности токена
 * @property accessToken Токен доступности
 * @property checkBlock Тогл проверки блокировки токена
 */
data class CheckAccessRqDto(
    val accessToken: String,
    val checkBlock: Boolean = false
)

/**
 * Dto-model Тело ответа на проверку активности токена
 * @property status Статус доступности токена
 * @property accessData Access данные
 */
data class CheckAccessRsDto(
    val status: AccessStatus = AccessStatus.BLOCKED,
    val accessData: AccessDataDto? = null
)  {

    /**
     * Статус активности токена
     */
    enum class AccessStatus {
        /**
         * Активный
         */
        ACTIVE,

        /**
         * Просроченный
         */
        EXPIRED,

        /**
         * Блокированный
         */
        BLOCKED
    }
}

