package st.special.sibyl.autoconfigure

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

/**
 * Enables structured JSON logging (ECS format) for non-local profiles.
 *
 * Active profiles alpha, staging, live → JSON (ECS) console output.
 * Local development (no profile or "local") → default plain-text logs.
 *
 * Apps can override by setting logging.structured.format.console explicitly.
 */
class SibylLoggingEnvironmentPostProcessor : EnvironmentPostProcessor {

    companion object {
        private val JSON_LOG_PROFILES = setOf("alpha", "staging", "live")
    }

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val activeProfiles = environment.activeProfiles.toSet()
        val shouldEnableJson = activeProfiles.any { it in JSON_LOG_PROFILES }

        if (shouldEnableJson) {
            val defaults = mapOf(
                "logging.structured.format.console" to "ecs",
            )
            environment.propertySources.addLast(
                MapPropertySource("sibylLoggingDefaults", defaults)
            )
        }
    }
}
