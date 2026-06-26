package no.novari.fint.explorer.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class RestClientConfig {

    @Bean
    fun restClient(): RestClient {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build()

        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(Duration.ofSeconds(15))
        }

        return RestClient.builder()
            .requestFactory(requestFactory)
            .defaultHeader("x-client", "fint-core-explorer")
            .build()
    }

}