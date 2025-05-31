package kr.hhplus.be.server.application.payment.event

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.domain.balance.event.BalanceDeductRequestedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUseRequestedEvent
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.payment.event.PaymentEventPublisher
import kr.hhplus.be.server.domain.payment.event.PaymentInitiatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentRollbackRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockDecreasedEvent
import kr.hhplus.be.server.domain.product.event.StockRollbackRequestedEvent
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PaymentOrchestratorTest {
    private val paymentEventPublisher = mockk<PaymentEventPublisher>(relaxed = true)
    private val orchestrator = PaymentOrchestrator(paymentEventPublisher, paymentEventPublisher)

    private val order = mockk<Order>(relaxed = true) {
        every { id } returns 1L
        every { totalPrice } returns 100_000
    }
    private val customerId = 123L
    private val couponId = 456L

    @Test
    @DisplayName("결제 시작 시 Initiated 이벤트 발행")
    fun publishPaymentInitiatedEvent_whenStartSaga() {
        // when
        orchestrator.startSage(order, customerId, couponId)

        // then
        verify(exactly = 1) {
            paymentEventPublisher.publish(match<PaymentInitiatedEvent> {
                it.order == order &&
                    it.customerId == customerId &&
                    it.couponId == couponId
            })
        }
    }

    @Test
    @DisplayName("쿠폰이 존재하면 CouponUseRequestedEvent 이벤트 발행")
    fun publishCouponUseRequestedEvent_whenCouponExists() {
        // given
        val event = StockDecreasedEvent(
            orderId = 1L,
            items = emptyList()
        )
        PaymentExecutionContextHolder.set(
            PaymentExecutionContext(order, customerId, couponId, 100_000)
        )

        // when
        orchestrator.handleStockDecreasedEvent(event)

        // then
        verify {
            paymentEventPublisher.publish(match<CouponUseRequestedEvent> {
                it.orderId == event.orderId && it.customerId == customerId
            })
        }
    }

    @Test
    @DisplayName("쿠폰이 없으면 BalanceDeductRequestedEvent 이벤트 발행")
    fun publishBalanceDeductEvent_whenNoCoupon() {
        // given
        val event = StockDecreasedEvent(
            orderId = 1L,
            items = emptyList()
        )
        PaymentExecutionContextHolder.set(
            PaymentExecutionContext(order, customerId, null, 100_000)
        )

        // when
        orchestrator.handleStockDecreasedEvent(event)

        // then
        verify {
            paymentEventPublisher.publish(match<BalanceDeductRequestedEvent> {
                it.orderId == event.orderId &&
                    it.customerId == customerId &&
                    it.totalPrice == 100_000
            })
        }
    }

    @Test
    @DisplayName("PaymentStep을 기반으로 rollback 이벤트를 역순으로 발행")
    fun rollback_shouldPublishRollbackEventsInReverseOrder() {
        // given
        val context = PaymentExecutionContext(order, customerId, couponId, 100_000)
        context.payment = mockk(relaxed = true) {
            every { id } returns 999L
        }
        context.completeStep(PaymentStep.STOCK_DECREASED)
        context.completeStep(PaymentStep.COUPON_APPLIED)
        context.completeStep(PaymentStep.BALANCE_DEDUCTED)
        context.completeStep(PaymentStep.PAYMENT_CREATED)

        PaymentExecutionContextHolder.set(context)

        // when
        orchestrator.rollback()

        // then
        verifyOrder {
            paymentEventPublisher.publish(any<PaymentRollbackRequestedEvent>())
            paymentEventPublisher.publish(any<BalanceRollbackRequestedEvent>())
            paymentEventPublisher.publish(any<CouponRollbackRequestedEvent>())
            paymentEventPublisher.publish(any<StockRollbackRequestedEvent>())
        }
    }
}
