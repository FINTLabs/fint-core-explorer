package no.novari.fint.explorer.support

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

suspend inline fun <reified T : Any> RestClient.fetch(uri: String, orgId: String? = null): T? =
    withContext(Dispatchers.IO) {
        get()
            .uri(uri)
            .apply { orgId?.let { header("x-org-id", it) } }
            .retrieve()
            .body<T>()
    }
