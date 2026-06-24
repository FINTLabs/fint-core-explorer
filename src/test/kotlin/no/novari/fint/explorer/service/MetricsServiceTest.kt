package no.novari.fint.explorer.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.fint.event.model.health.Health
import no.novari.fint.explorer.model.Asset
import no.novari.fint.explorer.model.CacheEntry
import no.novari.fint.explorer.model.SseOrg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux

private const val HEALTHY = "APPLICATION_HEALTHY"
private const val UNHEALTHY = "APPLICATION_UNHEALTHY"

class MetricsServiceTest {

    private lateinit var assetService: AssetService
    private lateinit var registry: SimpleMeterRegistry
    private lateinit var metricsService: MetricsService

    @BeforeEach
    fun setUp() {
        assetService = mock(AssetService::class.java)
        registry = SimpleMeterRegistry()
        metricsService = MetricsService(registry, assetService)
    }

    @Test
    fun `health metric is one when any status is healthy`() {
        val component = component("utdanning-vurdering").apply { health = listOf(Health(UNHEALTHY), Health(HEALTHY)) }

        update(asset("fylke-1", component))

        assertThat(healthGauge("fylke-1", "utdanning-vurdering")).isEqualTo(1.0)
    }

    @Test
    fun `health metric is zero when no status is healthy`() {
        val component = component("utdanning-vurdering").apply { health = listOf(Health(UNHEALTHY)) }

        update(asset("fylke-1", component))

        assertThat(healthGauge("fylke-1", "utdanning-vurdering")).isEqualTo(0.0)
    }

    @Test
    fun `health metric is zero when health is empty`() {
        update(asset("fylke-1", component("utdanning-vurdering")))

        assertThat(healthGauge("fylke-1", "utdanning-vurdering")).isEqualTo(0.0)
    }

    @Test
    fun `cache metric reports entry size per entity`() {
        val component = component("utdanning-vurdering").apply { cache = listOf(cacheEntry("elev", 42), cacheEntry("skole", 7)) }

        update(asset("fylke-1", component))

        assertThat(
            registry.get("fint.core.cache")
                .tags("asset", "fylke-1", "component", "utdanning-vurdering", "entity", "elev")
                .gauge().value(),
        ).isEqualTo(42.0)
        assertThat(
            registry.get("fint.core.cache")
                .tags("asset", "fylke-1", "component", "utdanning-vurdering", "entity", "skole")
                .gauge().value(),
        ).isEqualTo(7.0)
    }

    @Test
    fun `adapter event metric sums client events`() {
        val component = component("utdanning-vurdering").apply { clients = listOf(client(3), client(5)) }

        update(asset("fylke-1", component))

        assertThat(
            registry.get("fint.core.adapter.events.total")
                .tags("asset", "fylke-1", "component", "utdanning-vurdering")
                .gauge().value(),
        ).isEqualTo(8.0)
    }

    @Test
    fun `adapter connection metric counts clients`() {
        val component = component("utdanning-vurdering").apply { clients = listOf(client(3), client(5)) }

        update(asset("fylke-1", component))

        assertThat(
            registry.get("fint.core.adapter.connections")
                .tags("asset", "fylke-1", "component", "utdanning-vurdering")
                .gauge().value(),
        ).isEqualTo(2.0)
    }

    private fun update(vararg assets: Asset) {
        given(assetService.assets).willReturn(Flux.fromArray(assets))
        metricsService.update()
    }

    private fun healthGauge(asset: String, component: String) =
        registry.get("fint.core.health")
            .tags("asset", asset, "component", component)
            .gauge().value()

    private fun asset(id: String, vararg components: Asset.Component) = Asset().apply {
        this.id = id
        this.components = components.toMutableList()
    }

    private fun component(id: String) = Asset.Component().apply { this.id = id }

    private fun cacheEntry(name: String, size: Int) = CacheEntry().apply {
        this.name = name
        this.size = size
    }

    private fun client(events: Int) = SseOrg.SseClient().apply { this.events = events }
}
