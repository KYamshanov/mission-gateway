package ru.kyamshanov.mission.gateway

/**
 * Информация маршрута
 * @property pathPattern Шаблон для опрередления путки, к которому применяется роутинг
 * @property uri Uri куда будет отправлен запрос
 */
data class RoutData(val pathPattern: String, val uri: String)
