package no.novari.fint.explorer.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import no.fint.event.model.health.Health
import no.novari.fint.explorer.model.Asset
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Service
class MetricsService(
    private val meterRegistry: MeterRegistry,
    private val assetService: AssetService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val gauges = ConcurrentHashMap<String, AtomicInteger>()
    private val longGauges = ConcurrentHashMap<String, AtomicLong>()

    @Scheduled(initialDelayString = "\${fint.poll.initial-delay}", fixedDelayString = "\${fint.poll.fixed-delay}")
    fun update() {
        assetService.update()

        log.info("Start updating metrics...")
        assetService.getAssets().forEach { asset ->
            updateHealthMetric(asset)
            updateCacheMetric(asset)
            updateAdapterMetric(asset)
        }
        log.info("Finished updating metrics")
    }

    private fun updateHealthMetric(asset: Asset) {
        asset.components.forEach { component ->
            updateMetric(
                HEALTH_METRIC,
                listOf(Tag.of("asset", asset.id), Tag.of("component", component.id)),
                getHealthStatus(component.health),
            )
        }
    }

    private fun updateCacheMetric(asset: Asset) {
        asset.components.forEach { component ->
            component.cache.forEach { cacheEntry ->
                val tags = listOf(
                    Tag.of("asset", asset.id),
                    Tag.of("component", component.id),
                    Tag.of("entity", cacheEntry.name),
                )
                updateMetric(CACHE_METRIC, tags, cacheEntry.size)
                cacheEntry.lastUpdated?.let {
                    updateLongMetric(CACHE_LAST_UPDATED_METRIC, tags, it.toEpochSecond())
                }
            }
        }
    }

    private fun updateAdapterMetric(asset: Asset) {
        asset.components.forEach { component ->
            updateMetric(
                ADAPTER_EVENTS_METRIC_TOTAL,
                listOf(Tag.of("asset", asset.id), Tag.of("component", component.id)),
                component.clients.sumOf { it.events },
            )
            updateMetric(
                ADAPTER_CONNECTIONS_METRIC,
                listOf(Tag.of("asset", asset.id), Tag.of("component", component.id)),
                component.clients.size,
            )
        }
    }

    private fun getHealthStatus(healthList: List<Health>): Int =
        if (healthList.any { it.status == HEALTHY }) 1 else 0

    private fun updateMetric(metricType: String, tags: List<Tag>, value: Int) {
        val gaugeId = metricType + "-" + tags.joinToString("-") { it.value }
        gauges.computeIfPresent(gaugeId) { _, gauge -> gauge.apply { set(value) } }
        gauges.putIfAbsent(gaugeId, meterRegistry.gauge(metricType, tags, AtomicInteger(value)))
    }

    private fun updateLongMetric(metricType: String, tags: List<Tag>, value: Long) {
        val gaugeId = metricType + "-" + tags.joinToString("-") { it.value }
        longGauges.computeIfPresent(gaugeId) { _, gauge -> gauge.apply { set(value) } }
        longGauges.putIfAbsent(gaugeId, meterRegistry.gauge(metricType, tags, AtomicLong(value)))
    }

    companion object {
        private const val HEALTHY = "APPLICATION_HEALTHY"
        private const val HEALTH_METRIC = "fint.core.health"
        private const val CACHE_METRIC = "fint.core.cache"
        private const val CACHE_LAST_UPDATED_METRIC = "fint.core.cache.last.updated.seconds"
        private const val ADAPTER_CONNECTIONS_METRIC = "fint.core.adapter.connections"
        private const val ADAPTER_EVENTS_METRIC_TOTAL = "fint.core.adapter.events.total"
    }
}
