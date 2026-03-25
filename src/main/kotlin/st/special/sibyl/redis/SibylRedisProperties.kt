package st.special.sibyl.redis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sibyl.redis")
data class SibylRedisProperties(
    val lock: LockProperties = LockProperties(),
) {
    data class LockProperties(
        val prefix: String = "sibyl:lock:",
        val defaultTtlSeconds: Long = 30,
        val waitingPollIntervalMillis: Long = 100,
        val waitingTimeoutSeconds: Long = 10,
    )
}
