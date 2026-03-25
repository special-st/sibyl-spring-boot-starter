package st.special.sibyl.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate
import st.special.sibyl.redis.SibylLock
import st.special.sibyl.redis.SibylRedisProperties

@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate::class)
@EnableConfigurationProperties(SibylRedisProperties::class)
class SibylRedisAutoConfiguration {

    @Bean
    fun sibylLock(
        redisTemplate: StringRedisTemplate,
        properties: SibylRedisProperties,
    ): SibylLock = SibylLock(redisTemplate, properties)
}
