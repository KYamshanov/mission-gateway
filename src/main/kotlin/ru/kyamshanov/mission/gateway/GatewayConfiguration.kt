package ru.kyamshanov.mission.gateway

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.*
import ru.kyamshanov.mission.gateway.GlobalConstants.AUTHENTICATION_ROUT_DATA_QUALIFIER
import ru.kyamshanov.mission.gateway.GlobalConstants.ROUTS_CONFIG_RESOURCE

/**
 * Конфигурация приложения
 * @property authenticationPathPattern Патерн - путь для запросов к МС авторизации
 * @property authenticationUri Uri МС авторизации
 */
@Configuration
@PropertySources(PropertySource(value = [ROUTS_CONFIG_RESOURCE]))
class GatewayConfiguration(
    @Value("\${$AUTHENTICATION_PROPERTY.$PATH_PATTERN_KEY}")
    private val authenticationPathPattern: String,
    @Value("\${$AUTHENTICATION_PROPERTY.$URI_KEY}")
    private val authenticationUri: String
) {

    /**
     * Бин *singleton* [RoutData] для описания МС авторизации
     * Квалификатор бина - [AUTHENTICATION_ROUT_DATA_QUALIFIER]
     */
    @Bean
    @Qualifier(value = AUTHENTICATION_ROUT_DATA_QUALIFIER)
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun authenticationUri(): RoutData = RoutData(
        pathPattern = authenticationPathPattern,
        uri = authenticationUri
    )

    private companion object {

        const val AUTHENTICATION_PROPERTY = "authentication"
        const val PATH_PATTERN_KEY = "path_pattern"
        const val URI_KEY = "uri"
    }
}