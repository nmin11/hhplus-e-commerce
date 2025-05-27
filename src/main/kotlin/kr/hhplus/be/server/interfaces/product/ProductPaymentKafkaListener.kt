package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.product.ProductInfo
import kr.hhplus.be.server.domain.product.ProductRankService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ProductPaymentKafkaListener(
    private val productRankService: ProductRankService
) {
    companion object {
        private const val TOPIC_NAME = "payment-completed-topic"
        private const val GROUP_ID = "product-rank-consumer-group"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [TOPIC_NAME], groupId = GROUP_ID)
    fun listen(event: PaymentCompletedEvent, acknowledgment: Acknowledgment) {
        log.info("[Kafka] [ProductRank] 결제 완료 이벤트 수신")

        val increments = event.items.map {
            ProductInfo.SalesIncrement.from(it)
        }
        productRankService.increaseProductRanks(increments)

        log.info("[Kafka] [ProductRank] 상품 랭킹 정보 업데이트 완료")
        acknowledgment.acknowledge()
    }
}
