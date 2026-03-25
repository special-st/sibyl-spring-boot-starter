package st.special.sibyl.webclient

import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

class SibylWebClientFactory(
    private val properties: SibylWebClientProperties,
    private val webClientBuilder: WebClient.Builder,
) {
    private val cache = mutableMapOf<String, WebClient>()

    /**
     * Get a WebClient for a named service defined in `sibyl.webclient.services.*`.
     * Internal services get auth header relay automatically.
     */
    fun service(name: String): WebClient {
        return cache.getOrPut(name) {
            val config = properties.services[name]
                ?: throw IllegalArgumentException(
                    "Service '$name' not found in sibyl.webclient.services. " +
                        "Available: ${properties.services.keys}"
                )
            if (config.internal) internal(config.baseUrl) else external(config.baseUrl)
        }
    }

    /**
     * Create an internal WebClient with auth header relay.
     * Use for service-to-service calls within the cluster.
     */
    fun internal(baseUrl: String): WebClient {
        return webClientBuilder
            .clone()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(HttpClient.create()))
            .filter(AuthHeaderRelayFilter())
            .build()
    }

    /**
     * Create an external WebClient without header relay.
     * Use for calls to third-party APIs.
     */
    fun external(baseUrl: String): WebClient {
        return webClientBuilder
            .clone()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(HttpClient.create()))
            .build()
    }
}
