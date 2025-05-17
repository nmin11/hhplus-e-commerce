package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.product.ProductRankRepository
import kr.hhplus.be.server.event.ProductEvent
import kr.hhplus.be.server.infrastructure.product.ProductRankRedisEntry
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ProductSalesEventListener(
    private val productRankRepository: ProductRankRepository
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
            val productId = item.productId
            val quantity = item.quantity

            if (productRankRepository.existsRankKey(redisKey)) {
                productRankRepository.incrementProductSales(redisKey, productId, quantity)
            } else {
                val rankEntry = ProductRankRedisEntry(productId, quantity)
                productRankRepository.addRankEntry(redisKey, rankEntry, TTL)
            }
        }
    }
}
