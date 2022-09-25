package ru.kyamshanov.mission.gateway.configurations

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.reactive.function.client.WebClient

/**
 * Конфигурация веб
 */
@Configuration
class WebConfiguration {

    /**
     * Бин [WebClient]
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun webClientBean(): WebClient = WebClient.create()
}