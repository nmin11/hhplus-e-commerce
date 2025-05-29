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
        kafkaTemplate.send("inside.payment.initiated", event)
    }

    override fun publish(event: StockDecreaseRequestedEvent) {
        kafkaTemplate.send("inside.stock.decrease-requested", event)
    }

    override fun publish(event: CouponUseRequestedEvent) {
        kafkaTemplate.send("inside.coupon.use-requested", event)
    }

    override fun publish(event: BalanceDeductRequestedEvent) {
        kafkaTemplate.send("inside.balance.deduct-requested", event)
    }

    override fun publish(event: PaymentCreateRequestedEvent) {
        kafkaTemplate.send("inside.payment.create-requested", event)
    }

    override fun publish(event: PaymentCreatedEvent) {
        kafkaTemplate.send("inside.payment.created", event)
    }

    override fun publish(event: StatisticRecordRequestedEvent) {
        kafkaTemplate.send("inside.statistic.record-requested", event)
    }

    override fun publish(event: PaymentCompletedEvent) {
        kafkaTemplate.send("inside.payment.completed", event)
    }

    override fun publish(event: StockRollbackRequestedEvent) {
        kafkaTemplate.send("inside.stock.rollback-requested", event)
    }

    override fun publish(event: CouponRollbackRequestedEvent) {
        kafkaTemplate.send("inside.coupon.rollback-requested", event)
    }

    override fun publish(event: BalanceRollbackRequestedEvent) {
        kafkaTemplate.send("inside.balance.rollback-requested", event)
    }

    override fun publish(event: PaymentRollbackRequestedEvent) {
        kafkaTemplate.send("inside.payment.rollback-requested", event)
    }
}
