package kr.hhplus.be.server.domain.coupon.event

interface CouponEventPublisher {
    fun publish(event: CouponUsedEvent)
    fun publish(event: CouponIssuedEvent)
}
