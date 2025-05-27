package kr.hhplus.be.server.infrastructure.payment

import kr.hhplus.be.server.domain.balance.event.BalanceDeductRequestedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUseRequestedEvent
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreateRequestedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentEventPublisher
import kr.hhplus.be.server.domain.payment.event.PaymentInitiatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentRollbackRequestedEvent
import kr.hhplus.be.server.domain.product.event.StatisticRecordRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockDecreaseRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockRollbackRequestedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component("kafkaPaymentEventPublisher")
class KafkaPaymentEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : PaymentEventPublisher {
    override fun publish(event: PaymentInitiatedEvent) {
        kafkaTemplate.send("payment-initiated-topic", event)
    }

    override fun publish(event: StockDecreaseRequestedEvent) {
        kafkaTemplate.send("stock-decrease-requested-topic", event)
    }

    override fun publish(event: CouponUseRequestedEvent) {
        kafkaTemplate.send("coupon-use-requested-topic", event)
    }

    override fun publish(event: BalanceDeductRequestedEvent) {
        kafkaTemplate.send("balance-deduct-requested-topic", event)
    }

    override fun publish(event: PaymentCreateRequestedEvent) {
        kafkaTemplate.send("payment-create-requested-topic", event)
    }

    override fun publish(event: PaymentCreatedEvent) {
        kafkaTemplate.send("payment-created-topic", event)
    }

    override fun publish(event: StatisticRecordRequestedEvent) {
        kafkaTemplate.send("statistic-record-requested-topic", event)
    }

    override fun publish(event: PaymentCompletedEvent) {
        kafkaTemplate.send("payment-completed-topic", event)
    }

    override fun publish(event: StockRollbackRequestedEvent) {
        kafkaTemplate.send("stock-rollback-requested-topic", event)
    }

    override fun publish(event: CouponRollbackRequestedEvent) {
        kafkaTemplate.send("coupon-rollback-requested-topic", event)
    }

    override fun publish(event: BalanceRollbackRequestedEvent) {
        kafkaTemplate.send("balance-rollback-requested-topic", event)
    }

    override fun publish(event: PaymentRollbackRequestedEvent) {
        kafkaTemplate.send("payment-rollback-requested-topic", event)
    }
}
