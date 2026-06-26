package no.novari.fint.explorer.config

import no.novari.fint.explorer.model.Component
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@ConfigurationPropertiesBinding
class ComponentConverter : Converter<String, Component> {
    override fun convert(source: String): Component = Component(source)
}
