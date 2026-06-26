package no.novari.fint.explorer.service

import no.novari.fint.explorer.Endpoints.SSE_CLIENTS_ENDPOINT
import no.novari.fint.explorer.model.Component
import no.novari.fint.explorer.model.SseOrg
import no.novari.fint.explorer.support.fetch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ProviderService(
    private val restClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun getSseOrgs(component: Component): List<SseOrg> {
        val uri = component.providerUri(SSE_CLIENTS_ENDPOINT)
        return runCatching {
            restClient.fetch<List<SseOrg>>(uri)
        }.getOrElse {
            log.warn("Failed to fetch SSE clients from {}: {}", uri, it.message)
            null
        } ?: emptyList()
    }
}
