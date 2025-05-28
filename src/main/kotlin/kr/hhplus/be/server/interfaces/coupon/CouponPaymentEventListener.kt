package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.domain.coupon.event.CouponEventPublisher
import kr.hhplus.be.server.domain.coupon.event.CouponUseRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUsedEvent
import kr.hhplus.be.server.domain.coupon.CustomerCouponService
import kr.hhplus.be.server.domain.coupon.event.CouponRollbackRequestedEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponPaymentEventListener(
    private val customerCouponService: CustomerCouponService,
    @Qualifier("springCouponEventPublisher")
    private val couponEventPublisher: CouponEventPublisher
) {
    @Transactional
    @EventListener
    fun handleUse(event: CouponUseRequestedEvent) {
        val customerCoupon = customerCouponService.validateIssuedCoupon(
            event.customerId,
            event.couponId
        )
        val coupon = customerCoupon.coupon

        coupon.validatePeriod()
        customerCouponService.markAsUsed(customerCoupon)

        val discountAmount = coupon.calculateDiscount(event.totalPrice)
        val discountedPrice = event.totalPrice - discountAmount

        val couponUsedEvent = CouponUsedEvent(event.orderId, discountAmount, discountedPrice)
        couponEventPublisher.publish(couponUsedEvent)
    }

    @Transactional
    @EventListener
    fun handleRollback(event: CouponRollbackRequestedEvent) {
        val customerCoupon = customerCouponService.getIssuedCoupon(
            event.customerId,
            event.couponId
        )

        customerCouponService.rollbackUse(customerCoupon)
    }
}
