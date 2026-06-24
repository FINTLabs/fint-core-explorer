package no.novari.fint.explorer.service

import no.fint.event.model.Event
import no.fint.event.model.health.Health
import no.novari.fint.explorer.Endpoints.ADMIN_CACHE_STATUS_ENDPOINT
import no.novari.fint.explorer.Endpoints.ADMIN_HEALTH_ENDPOINT
import no.novari.fint.explorer.model.CacheEntry
import no.novari.fint.explorer.model.Component
import no.novari.fint.explorer.support.fetch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ConsumerService(
    private val restClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun getHealth(component: Component, orgId: String): List<Health> {
        val uri = component.consumerUri(ADMIN_HEALTH_ENDPOINT)
        return runCatching {
            restClient.fetch<Event<Health>>(uri, orgId)?.data
        }.getOrElse {
            log.warn("Failed to fetch health from {} for {}: {}", uri, orgId, it.message)
            null
        } ?: emptyList()
    }

    suspend fun getCache(component: Component): Map<String, Map<String, CacheEntry>> {
        val uri = component.consumerUri(ADMIN_CACHE_STATUS_ENDPOINT)
        return runCatching {
            restClient.fetch<Map<String, Map<String, CacheEntry>>>(uri)
        }.getOrElse {
            log.warn("Failed to fetch cache from {}: {}", uri, it.message)
            null
        } ?: emptyMap()
    }
}
