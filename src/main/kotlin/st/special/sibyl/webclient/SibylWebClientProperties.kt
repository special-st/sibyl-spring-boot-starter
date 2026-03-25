package st.special.sibyl.webclient

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sibyl.webclient")
data class SibylWebClientProperties(
    val services: Map<String, ServiceProperties> = emptyMap(),
) {
    data class ServiceProperties(
        val baseUrl: String,
        val internal: Boolean = false,
    )
}
