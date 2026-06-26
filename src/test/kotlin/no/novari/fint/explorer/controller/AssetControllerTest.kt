package no.novari.fint.explorer.controller

import no.novari.fint.explorer.model.Asset
import no.novari.fint.explorer.service.AssetService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(AssetController::class)
class AssetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var assetService: AssetService

    @Test
    fun `GET assets returns all`() {
        given(assetService.getAssets()).willReturn(listOf(asset("fylke-1"), asset("fylke-2")))

        mockMvc.get("/assets")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.length()") { value(2) } }
    }

    @Test
    fun `GET asset returns asset when present`() {
        given(assetService.getAsset("fylke-1")).willReturn(asset("fylke-1"))

        mockMvc.get("/assets/fylke-1")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.id") { value("fylke-1") } }
    }

    @Test
    fun `GET asset returns 404 when absent`() {
        given(assetService.getAsset("missing")).willReturn(null)

        mockMvc.get("/assets/missing")
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `GET components returns all when no id filter`() {
        given(assetService.getAsset("fylke-1")).willReturn(asset("fylke-1", "utdanning-vurdering", "administrasjon-personal"))

        mockMvc.get("/assets/fylke-1/components")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.length()") { value(2) } }
    }

    @Test
    fun `GET components filters by id substring`() {
        given(assetService.getAsset("fylke-1")).willReturn(asset("fylke-1", "utdanning-vurdering", "administrasjon-personal"))

        mockMvc.get("/assets/fylke-1/components") { param("id", "utdanning") }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.length()") { value(1) } }
            .andExpect { jsonPath("$[0].id") { value("utdanning-vurdering") } }
    }

    @Test
    fun `GET components returns 404 for unknown asset`() {
        given(assetService.getAsset("missing")).willReturn(null)

        mockMvc.get("/assets/missing/components")
            .andExpect { status { isNotFound() } }
    }

    private fun asset(id: String, vararg componentIds: String) = Asset().apply {
        this.id = id
        componentIds.forEach { cid -> components.add(Asset.ComponentStatus().apply { this.id = cid }) }
    }
}
