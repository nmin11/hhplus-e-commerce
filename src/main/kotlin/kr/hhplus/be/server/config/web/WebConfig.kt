package kr.hhplus.be.server.config.web

import kr.hhplus.be.server.support.customercontext.CustomerContextInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val customerContextInterceptor: CustomerContextInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(customerContextInterceptor)
            .addPathPatterns("/**")
    }
}
