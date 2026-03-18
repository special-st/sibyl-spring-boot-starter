package st.special.sibyl.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sibyl.monitoring")
data class SibylMonitoringProperties(
    /** Service name tag added to all metrics */
    val serviceName: String = "unknown",
    /** Environment tag (prod, staging, dev) */
    val environment: String = "prod",
    /** Enable RED metrics (Rate, Errors, Duration) for HTTP endpoints */
    val red: Boolean = true,
    /** Enable USE metrics (Utilization, Saturation, Errors) for resources */
    val use: Boolean = true,
)
