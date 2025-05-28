package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.event.CouponEventPublisher
import kr.hhplus.be.server.domain.coupon.event.CouponIssuedEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class CouponFacade(
    private val couponService: CouponService,
    @Qualifier("kafkaCouponEventPublisher")
    private val couponEventPublisher: CouponEventPublisher
) {
    fun issueCouponToCustomer(command: CouponCommand.Issue): CouponResult.Issue {
        val (couponId, customerId) = command

        // Redis를 활용한 쿠폰 발급 처리
        couponService.issue(couponId, customerId)

        // 쿠폰 발급 이벤트 발송
        couponEventPublisher.publish(
            CouponIssuedEvent(couponId, customerId)
        )

        return CouponResult.Issue(couponId, customerId)
    }
}
