package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.event.ProductEvent
import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.infrastructure.redis.RedisSortedSetRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ProductSalesEventListener(
    private val redisRepository: RedisRepository,
    private val redisSortedSetRepository: RedisSortedSetRepository
) {
    companion object {
        private val TTL = Duration.ofDays(8)
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(command: ProductEvent.SalesUpdated) {
        val today = dateFormatter.format(LocalDate.now())
        val redisKey = "product:sales:$today"

        command.items.forEach { item ->
            val productId = item.productId.toString()
            val quantity = item.quantity.toDouble()

            if (redisRepository.exists(redisKey)) {
                redisSortedSetRepository.incrementScore(redisKey, productId, quantity)
            } else {
                redisSortedSetRepository.add(redisKey, productId, quantity, TTL)
            }
        }
    }
}
