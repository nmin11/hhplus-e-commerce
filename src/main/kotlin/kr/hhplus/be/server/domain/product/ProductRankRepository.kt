package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infrastructure.product.ProductRankRedisEntry
import java.time.Duration

interface ProductRankRepository {
    fun existsRankKey(redisKey: String): Boolean
    fun getTopNWithSalesCount(redisKey: String, count: Int): List<ProductRankRedisEntry>
    fun addRankEntry(redisKey: String, entry: ProductRankRedisEntry, ttl: Duration)
    fun incrementProductSales(redisKey: String, productId: Long, quantity: Int)
    fun unionRanks(sourceKeys: List<String>, destinationKey: String, ttl: Duration)
}
