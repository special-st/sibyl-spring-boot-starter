package st.special.sibyl.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import st.special.sibyl.auth.AuthUserArgumentResolver

@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer::class)
class SibylAuthAutoConfiguration : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(AuthUserArgumentResolver())
    }
}
