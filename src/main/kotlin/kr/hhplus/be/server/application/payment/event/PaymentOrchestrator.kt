package kr.hhplus.be.server.application.payment.event

import kr.hhplus.be.server.application.payment.PaymentResult
import kr.hhplus.be.server.domain.balance.event.BalanceDeductRequestedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceDeductedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUseRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUsedEvent
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreateRequestedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentEventPublisher
import kr.hhplus.be.server.domain.payment.event.PaymentInitiatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentRollbackRequestedEvent
import kr.hhplus.be.server.domain.product.ProductInfo
import kr.hhplus.be.server.domain.product.event.StatisticRecordRequestedEvent
import kr.hhplus.be.server.domain.product.event.StatisticRecordedEvent
import kr.hhplus.be.server.domain.product.event.StockDecreaseRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockDecreasedEvent
import kr.hhplus.be.server.domain.product.event.StockRollbackRequestedEvent
import kr.hhplus.be.server.support.exception.payment.PaymentResultNotReadyException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PaymentOrchestrator(
    private val paymentEventPublisher: PaymentEventPublisher,
    @Qualifier("kafkaPaymentEventPublisher")
    private val kafkaPaymentEventPublisher: PaymentEventPublisher
) {
    private val log = LoggerFactory.getLogger(PaymentOrchestrator::class.java)

    fun startSage(order: Order, customerId: Long, couponId: Long?) {
        val initEvent = PaymentInitiatedEvent(
            order,
            customerId,
            couponId
        )
        try {
            paymentEventPublisher.publish(initEvent)
        } catch (e: Exception) {
            log.error(e.message)
            rollback()
            throw e
        }
    }

    @EventListener
    fun handlePaymentInitiatedEvent(event: PaymentInitiatedEvent) {
        val context = PaymentExecutionContext(
            event.order,
            event.customerId,
            event.couponId,
            event.order.totalPrice
        )
        PaymentExecutionContextHolder.set(context)
        log.info("[Orchestrator] 결제 처리 시작")

        val stockDecreaseEvent = StockDecreaseRequestedEvent.from(event.order)
        paymentEventPublisher.publish(stockDecreaseEvent)
    }

    @EventListener
    fun handleStockDecreasedEvent(event: StockDecreasedEvent) {
        val context = PaymentExecutionContextHolder.get()
        context.stockDecreased = event.items
        context.completeStep(PaymentStep.STOCK_DECREASED)

        if (context.hasCoupon) {
            val couponUseEvent = CouponUseRequestedEvent(
                orderId = event.orderId,
                customerId = context.customerId,
                couponId = requireNotNull(context.couponId),
                totalPrice = context.totalPrice
            )
            paymentEventPublisher.publish(couponUseEvent)
        } else {
            val balanceDeductEvent = BalanceDeductRequestedEvent(
                orderId = event.orderId,
                customerId = context.customerId,
                totalPrice = context.totalPrice
            )
            paymentEventPublisher.publish(balanceDeductEvent)
        }
        log.info("[Orchestrator] 재고 차감 완료")
    }

    @EventListener
    fun handleCouponUsedEvent(event: CouponUsedEvent) {
        val context = PaymentExecutionContextHolder.get()
        context.discountAmount = event.discountAmount
        context.completeStep(PaymentStep.COUPON_APPLIED)

        val balanceDeductEvent = BalanceDeductRequestedEvent(
            orderId = event.orderId,
            customerId = context.customerId,
            totalPrice = context.totalPrice - context.discountAmount
        )
        paymentEventPublisher.publish(balanceDeductEvent)
        log.info("[Orchestrator] 쿠폰 사용 처리 완료")
    }

    @EventListener
    fun handleBalanceDeductedEvent(event: BalanceDeductedEvent) {
        val context = PaymentExecutionContextHolder.get()
        context.completeStep(PaymentStep.BALANCE_DEDUCTED)

        val paymentCreateEvent = PaymentCreateRequestedEvent(
            orderId = event.orderId,
            customerId = context.customerId,
            couponId = context.couponId,
            originalPrice = context.totalPrice,
            discountAmount = context.discountAmount
        )
        paymentEventPublisher.publish(paymentCreateEvent)
        log.info("[Orchestrator] 잔액 차감 처리 완료")
    }

    @EventListener
    fun handlePaymentCreatedEvent(event: PaymentCreatedEvent) {
        val context = PaymentExecutionContextHolder.get()
        context.payment = event.payment
        context.completeStep(PaymentStep.PAYMENT_CREATED)

        val statisticRecordEvent = StatisticRecordRequestedEvent.from(context.order)
        paymentEventPublisher.publish(statisticRecordEvent)
        log.info("[Orchestrator] 상품 통계 데이터 저장 완료")
    }

    @EventListener
    fun handleStatisticRecordedEvent(event: StatisticRecordedEvent) {
        val context = PaymentExecutionContextHolder.get()
        context.completeStep(PaymentStep.STATISTIC_RECORDED)

        val paymentCompletedEvent = PaymentCompletedEvent.from(context.order)
        kafkaPaymentEventPublisher.publish(paymentCompletedEvent)
        log.info("[Orchestrator] 결제 완료")
    }

    fun getResult(): PaymentResult {
        val context = PaymentExecutionContextHolder.get()
        val result = context.payment?.let {
            PaymentResult.Companion.from(it)
        } ?: throw PaymentResultNotReadyException()
        PaymentExecutionContextHolder.clear()
        return result
    }

    fun rollback() {
        log.warn("[Orchestrator] rollback 작업")
        val context = PaymentExecutionContextHolder.get()

        context.completedSteps.reversed().forEach { step ->
            when(step) {
                PaymentStep.PAYMENT_CREATED -> {
                    log.warn("[Orchestrator] 결제 내역 rollback")
                    paymentEventPublisher.publish(PaymentRollbackRequestedEvent(
                        paymentId = requireNotNull(context.payment).id
                    ))
                }

                PaymentStep.BALANCE_DEDUCTED -> {
                    log.warn("[Orchestrator] 잔액 차감 rollback")
                    paymentEventPublisher.publish(BalanceRollbackRequestedEvent(
                        context.customerId,
                        context.totalPrice - context.discountAmount
                    ))
                }

                PaymentStep.COUPON_APPLIED -> {
                    log.warn("[Orchestrator] 쿠폰 사용 rollback")
                    paymentEventPublisher.publish(CouponRollbackRequestedEvent(
                        customerId = context.customerId,
                        couponId = requireNotNull(context.couponId)
                    ))
                }

                PaymentStep.STOCK_DECREASED -> {
                    log.warn("[Orchestrator] 재고 차감 rollback")
                    paymentEventPublisher.publish(StockRollbackRequestedEvent(
                        orderId = context.order.id,
                        items = ProductInfo.StockItem.from(context.order)
                    ))
                }

                else -> log.warn("[Orchestrator] rollback 대상에 없음: $step")
            }
        }

        PaymentExecutionContextHolder.clear()
    }
}
