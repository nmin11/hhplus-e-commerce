package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.ProductRankRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ProductRankRepositoryImpl(
    private val stringRedisTemplate: StringRedisTemplate
) : ProductRankRepository {
    override fun existsRankKey(redisKey: String): Boolean {
        return stringRedisTemplate.hasKey(redisKey)
    }

    override fun getTopNWithSalesCount(redisKey: String, count: Int): List<ProductRankRedisEntry> {
        return stringRedisTemplate
            .opsForZSet()
            .reverseRangeWithScores(redisKey, 0, count - 1L)
            .orEmpty()
            .mapNotNull {
                val id = it.value?.toLongOrNull()
                val score = it.score?.toInt()
                if (id != null && score != null) ProductRankRedisEntry(id, score) else null
            }
    }

    override fun addRankEntry(
        redisKey: String,
        entry: ProductRankRedisEntry,
        ttl: Duration
    ) {
        stringRedisTemplate
            .opsForZSet()
            .add(redisKey, entry.productId.toString(), entry.totalSales.toDouble())
        stringRedisTemplate.expire(redisKey, ttl)
    }

    override fun incrementProductSales(redisKey: String, productId: Long, quantity: Int) {
        stringRedisTemplate
            .opsForZSet()
            .incrementScore(redisKey, productId.toString(), quantity.toDouble())
    }

    override fun unionRanks(
        sourceKeys: List<String>,
        destinationKey: String,
        ttl: Duration
    ) {
        stringRedisTemplate
            .opsForZSet()
            .unionAndStore(sourceKeys.first(), sourceKeys.drop(1), destinationKey)
        stringRedisTemplate.expire(destinationKey, ttl)
    }
}
