package no.novari.fint.explorer.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import no.novari.fint.explorer.config.FintProperties
import no.novari.fint.explorer.factory.toAsset
import no.novari.fint.explorer.model.Asset
import no.novari.fint.explorer.model.Component
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentSkipListMap

@Service
class AssetService(
    private val providerService: ProviderService,
    private val consumerService: ConsumerService,
    private val fintProperties: FintProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val assets = ConcurrentSkipListMap<String, Asset>()

    fun getAssets(): Flux<Asset> = Flux.fromIterable(assets.values)

    fun getAsset(id: String): Mono<Asset> = Mono.justOrEmpty(assets[id])

    fun update() {
        log.info("Start collect data...")
        runBlocking { collect() }.forEach { assets[it.id] = it }
        log.info("Finished collecting data")
    }

    private suspend fun collect(): List<Asset> = coroutineScope {
        fintProperties.components
            .map { component -> async { providerService.getSseOrgs(component) } }
            .awaitAll()
            .flatten()
            .groupBy { it.orgId }
            .map { entry ->
                async {
                    val asset = entry.toAsset()
                    asset.components.map { status -> async { enrich(asset.id, status) } }.awaitAll()
                    asset
                }
            }
            .awaitAll()
    }

    private suspend fun enrich(orgId: String, status: Asset.ComponentStatus) {
        status.lastUpdated = ZonedDateTime.now(ZoneId.of("Z"))

        if (status.clients.isEmpty()) {
            return
        }

        val component = Component(status.id)
        status.health = consumerService.getHealth(component, orgId)
        status.cache = consumerService.getCache(component)
            .getOrDefault(orgId, emptyMap())
            .map { (entity, entry) -> entry.apply { name = entity } }
    }
}
