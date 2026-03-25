package st.special.sibyl.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import st.special.sibyl.webclient.SibylWebClientFactory
import st.special.sibyl.webclient.SibylWebClientProperties

@AutoConfiguration
@ConditionalOnClass(WebClient::class)
@EnableConfigurationProperties(SibylWebClientProperties::class)
class SibylWebClientAutoConfiguration {

    @Bean
    fun sibylWebClientFactory(
        properties: SibylWebClientProperties,
        webClientBuilder: WebClient.Builder,
    ): SibylWebClientFactory = SibylWebClientFactory(properties, webClientBuilder)
}
