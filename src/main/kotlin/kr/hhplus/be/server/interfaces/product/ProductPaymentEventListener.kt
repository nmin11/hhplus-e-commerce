package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.product.ProductInfo
import kr.hhplus.be.server.domain.product.ProductRankService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ProductPaymentEventListener(
    private val productRankService: ProductRankService
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PaymentCompletedEvent) {
        val items = event.items
        val increments = items.map {
            ProductInfo.SalesIncrement.from(it)
        }

        productRankService.increaseProductRanks(increments)
    }
}
