package no.novari.fint.explorer.factory

import no.novari.fint.explorer.model.SseOrg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AssetFactoryTest {

    @Test
    fun `uses orgId as asset id`() {
        val asset = java.util.Map.entry("fylke-1", listOf(sseOrg("/utdanning/vurdering/provider/sse/clients"))).toAsset()

        assertThat(asset.id).isEqualTo("fylke-1")
    }

    @Test
    fun `builds one component per sseOrg`() {
        val entry = java.util.Map.entry(
            "fylke-1",
            listOf(
                sseOrg("/utdanning/vurdering/provider/sse/clients"),
                sseOrg("/administrasjon/personal/provider/sse/clients"),
            ),
        )

        val asset = entry.toAsset()

        assertThat(asset.components).hasSize(2)
    }

    @Test
    fun `derives component id from path between root and provider`() {
        val asset = java.util.Map.entry("fylke-1", listOf(sseOrg("/utdanning/vurdering/provider/sse/clients"))).toAsset()

        assertThat(asset.components.first().id).isEqualTo("utdanning-vurdering")
    }

    @Test
    fun `derives capitalized spaced title from path`() {
        val asset = java.util.Map.entry("fylke-1", listOf(sseOrg("/utdanning/vurdering/provider/sse/clients"))).toAsset()

        assertThat(asset.components.first().title).isEqualTo("Utdanning Vurdering")
    }

    @Test
    fun `carries clients onto component`() {
        val org = sseOrg("/utdanning/vurdering/provider/sse/clients")
        org.clients = listOf(client(3), client(5))

        val asset = java.util.Map.entry("fylke-1", listOf(org)).toAsset()

        assertThat(asset.components.first().clients).hasSize(2)
    }

    @Test
    fun `keeps interior empty segment from double slash like Java`() {
        val asset = java.util.Map.entry("fylke-1", listOf(sseOrg("/utdanning//vurdering/provider/sse"))).toAsset()

        assertThat(asset.components.first().id).isEqualTo("utdanning--vurdering")
        assertThat(asset.components.first().title).isEqualTo("Utdanning  Vurdering")
    }

    @Test
    fun `drops trailing empty segment like Java`() {
        val asset = java.util.Map.entry("fylke-1", listOf(sseOrg("/utdanning//provider/sse"))).toAsset()

        assertThat(asset.components.first().id).isEqualTo("utdanning-")
        assertThat(asset.components.first().title).isEqualTo("Utdanning")
    }

    @Test
    fun `throws when path has no provider segment`() {
        val entry = java.util.Map.entry("fylke-1", listOf(sseOrg("/utdanning/vurdering")))

        assertThrows<NullPointerException> { entry.toAsset() }
    }

    private fun sseOrg(path: String) = SseOrg().apply { this.path = path }

    private fun client(events: Int) = SseOrg.SseClient().apply { this.events = events }
}
