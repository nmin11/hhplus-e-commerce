package kr.hhplus.be.server.domain.payment.event

import kr.hhplus.be.server.domain.balance.event.BalanceDeductRequestedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUseRequestedEvent
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.product.event.StatisticRecordRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockDecreaseRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockRollbackRequestedEvent

interface PaymentEventPublisher {
    fun publish(event: PaymentInitiatedEvent)
    fun publish(event: StockDecreaseRequestedEvent)
    fun publish(event: CouponUseRequestedEvent)
    fun publish(event: BalanceDeductRequestedEvent)
    fun publish(event: PaymentCreateRequestedEvent)
    fun publish(event: PaymentCreatedEvent)
    fun publish(event: StatisticRecordRequestedEvent)
    fun publish(event: PaymentCompletedEvent)

    fun publish(event: StockRollbackRequestedEvent)
    fun publish(event: CouponRollbackRequestedEvent)
    fun publish(event: BalanceRollbackRequestedEvent)
    fun publish(event: PaymentRollbackRequestedEvent)
}
