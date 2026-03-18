package st.special.sibyl.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SibylMetricsConfig {

    // JVM metrics (always enabled)
    @Bean fun sibylJvmMemory() = JvmMemoryMetrics()
    @Bean fun sibylJvmGc() = JvmGcMetrics()
    @Bean fun sibylJvmThreads() = JvmThreadMetrics()
    @Bean fun sibylProcessor() = ProcessorMetrics()
    @Bean fun sibylUptime() = UptimeMetrics()

    // Custom business metrics helper
    @Bean
    fun sibylMetrics(registry: MeterRegistry) = SibylMetrics(registry)
}
