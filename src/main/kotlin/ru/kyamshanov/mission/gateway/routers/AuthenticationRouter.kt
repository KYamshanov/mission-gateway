package ru.kyamshanov.mission.gateway.routers

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.kyamshanov.mission.gateway.GlobalConstants.AUTHENTICATION_ROUT_DATA_QUALIFIER
import ru.kyamshanov.mission.gateway.RoutData


/**
 * Настройка роутинга для сервера авторизации
 * @property authenticationRoutData Информация для настройки роутинга
 */
@Configuration
class AuthenticationRouter(
    @Qualifier(value = AUTHENTICATION_ROUT_DATA_QUALIFIER)
    private val authenticationRoutData: RoutData
) {

    /**
     * Бин-RouteLocator
     */
    @Bean
    fun authenticationRouteLocator(builder: RouteLocatorBuilder): RouteLocator =
        builder.routes()
            .route { r -> r.path(authenticationRoutData.pathPattern).uri(authenticationRoutData.uri) }
            .build()
}