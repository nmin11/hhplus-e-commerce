package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.event.CouponEventPublisher
import kr.hhplus.be.server.domain.coupon.event.CouponIssuedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUsedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component("springCouponEventPublisher")
class SpringCouponEventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) : CouponEventPublisher {
    override fun publish(event: CouponUsedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: CouponIssuedEvent) {
        eventPublisher.publishEvent(event)
    }
}
