package no.novari.fint.explorer.controller

import no.novari.fint.explorer.exception.AssetNotFoundException
import no.novari.fint.explorer.model.Asset
import no.novari.fint.explorer.service.AssetService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AssetControllerTest {

    private lateinit var assetService: AssetService
    private lateinit var controller: AssetController

    @BeforeEach
    fun setUp() {
        assetService = mock(AssetService::class.java)
        controller = AssetController(assetService)
    }

    @Test
    fun `getAssets streams all`() {
        given(assetService.assets).willReturn(Flux.just(Asset(), Asset()))

        StepVerifier.create(controller.assets)
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `getAsset returns asset when present`() {
        val asset = asset("fylke-1")
        given(assetService.getAsset("fylke-1")).willReturn(Mono.just(asset))

        StepVerifier.create(controller.getAsset("fylke-1"))
            .expectNext(asset)
            .verifyComplete()
    }

    @Test
    fun `getAsset errors with not found when absent`() {
        given(assetService.getAsset("missing")).willReturn(Mono.empty())

        StepVerifier.create(controller.getAsset("missing"))
            .expectError(AssetNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getComponents returns all when no id filter`() {
        given(assetService.getAsset("fylke-1")).willReturn(Mono.just(asset("fylke-1", "utdanning-vurdering", "administrasjon-personal")))

        StepVerifier.create(controller.getComponents("fylke-1", null))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `getComponents filters by id substring`() {
        given(assetService.getAsset("fylke-1")).willReturn(Mono.just(asset("fylke-1", "utdanning-vurdering", "administrasjon-personal")))

        StepVerifier.create(controller.getComponents("fylke-1", "utdanning"))
            .expectNextMatches { it.id == "utdanning-vurdering" }
            .verifyComplete()
    }

    @Test
    fun `getComponents propagates not found for unknown asset`() {
        given(assetService.getAsset("missing")).willReturn(Mono.empty())

        StepVerifier.create(controller.getComponents("missing", null))
            .expectError(AssetNotFoundException::class.java)
            .verify()
    }

    private fun asset(id: String, vararg componentIds: String) = Asset().apply {
        this.id = id
        componentIds.forEach { cid -> components.add(Asset.ComponentStatus().apply { this.id = cid }) }
    }
}
