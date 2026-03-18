package st.special.sibyl.autoconfigure

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import st.special.sibyl.metrics.SibylMetricsConfig

@AutoConfiguration
@ConditionalOnClass(MeterRegistry::class)
@EnableConfigurationProperties(SibylMonitoringProperties::class)
@Import(SibylMetricsConfig::class)
class SibylMonitoringAutoConfiguration {

    @Bean
    fun sibylMeterRegistryCustomizer(
        props: SibylMonitoringProperties
    ): MeterRegistryCustomizer<MeterRegistry> = MeterRegistryCustomizer { registry ->
        registry.config()
            .commonTags("service", props.serviceName)
            .commonTags("env", props.environment)
    }
}
