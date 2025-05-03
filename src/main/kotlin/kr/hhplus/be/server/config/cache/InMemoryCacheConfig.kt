package kr.hhplus.be.server.config.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class InMemoryCacheConfig {
    @Bean
    fun caffeineCache(): Cache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .initialCapacity(100)
        .maximumSize(1_000)
        .softValues()
        .build()
}
