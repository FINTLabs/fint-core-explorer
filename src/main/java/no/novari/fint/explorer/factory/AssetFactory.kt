package no.novari.fint.explorer.factory

import no.novari.fint.explorer.model.Asset
import no.novari.fint.explorer.model.Asset.ComponentStatus
import no.novari.fint.explorer.model.SseOrg
import org.apache.commons.lang3.StringUtils

private const val PROVIDER = "/provider"

fun Map.Entry<String, List<SseOrg>>.toAsset(): Asset =
    Asset().apply {
        id = key
        components = value.toComponentStatus()
    }

private fun List<SseOrg>.toComponentStatus(): List<ComponentStatus> =
    map { sseOrg ->
        ComponentStatus().apply {
            id = sseOrg.path.toComponentId()
            title = sseOrg.path.toComponentTitle()
            clients = sseOrg.clients
        }
    }

private fun String.toComponentId(): String =
    StringUtils.substringBetween(this, "/", PROVIDER)!!.replace("/", "-")

private fun String.toComponentTitle(): String =
    StringUtils.substringBetween(this, "/", PROVIDER)!!
        .replace("/", " ")
        .split(" ")
        .dropLastWhile { it.isEmpty() }
        .joinToString(" ") { StringUtils.capitalize(it) }
