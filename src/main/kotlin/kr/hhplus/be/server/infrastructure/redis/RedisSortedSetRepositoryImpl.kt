package kr.hhplus.be.server.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisSortedSetRepositoryImpl(
    private val stringRedisTemplate: StringRedisTemplate
) : RedisSortedSetRepository {
    override fun add(key: String, member: String, score: Double, ttl: Duration?) {
        stringRedisTemplate
            .opsForZSet()
            .add(key, member, score)

        ttl?.let { stringRedisTemplate.expire(key, it) }
    }

    override fun incrementScore(key: String, member: String, score: Double) {
        stringRedisTemplate
            .opsForZSet()
            .incrementScore(key, member, score)
    }

    override fun unionAndStore(
        sourceKeys: List<String>,
        destinationKey: String,
        ttl: Duration?
    ) {
        stringRedisTemplate
            .opsForZSet()
            .unionAndStore(sourceKeys.first(), sourceKeys.drop(1), destinationKey)

        ttl?.let { stringRedisTemplate.expire(destinationKey, it) }
    }
}
