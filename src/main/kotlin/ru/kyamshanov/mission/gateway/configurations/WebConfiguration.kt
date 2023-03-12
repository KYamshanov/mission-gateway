package ru.kyamshanov.mission.gateway.configurations

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import ru.kyamshanov.mission.gateway.web.WebClientFactory

/**
 * Конфигурация веб
 */
@Configuration
internal class WebConfiguration(
    private val webClientFactory: WebClientFactory
) {

    /**
     * Бин [WebClient]
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun webClientBean(): WebClient = webClientFactory.create()
}