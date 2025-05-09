package kr.hhplus.be.server.config.redis

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig(
    @Value("\${spring.redis.host}") private val redisHost: String,
    @Value("\${spring.redis.port}") private val redisPort: Int,
    @Value("\${spring.redis.password}") private val redisPassword: String
) {
    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()

        config.useSingleServer()
            .setAddress("redis://$redisHost:$redisPort")
            .setPassword(redisPassword)

        return Redisson.create(config)
    }
}
