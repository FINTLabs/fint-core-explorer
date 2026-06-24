package no.novari.fint.explorer.model

@JvmInline
value class Component(
    private val value: String
) {
    init {
        require(value.split("-").size == 2) { "Component is expected to have exactly 2 parts (domain-package), got: $value" }
        require(value == value.lowercase()) { "Component is expected to be lowercase, got: $value" }
    }

    val domainName: String get() = value.split("-").first()
    val packageName: String get() = value.split("-").last()

    private val path: String get() = "/$domainName/$packageName"

    fun providerUri(endpoint: String) = "http://provider-$value:$PORT$path$endpoint"

    fun consumerUri(endpoint: String) = "http://consumer-$value:$PORT$path$endpoint"

    private companion object {
        const val PORT = 8080
    }
}