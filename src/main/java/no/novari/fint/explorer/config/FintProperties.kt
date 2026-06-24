package no.novari.fint.explorer.config

import no.novari.fint.explorer.model.Component
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * [components] is the list of components we are going to perform health checks on.
 */
@ConfigurationProperties("fint")
data class FintProperties(
    val components: List<Component>
)