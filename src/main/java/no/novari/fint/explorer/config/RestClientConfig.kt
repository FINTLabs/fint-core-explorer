package no.novari.fint.explorer.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    fun restClient() =
        RestClient.builder()
            .defaultHeader("x-client", "fint-core-explorer")
            .build()

}