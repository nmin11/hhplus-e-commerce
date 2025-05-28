package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CustomerCouponService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CouponScheduler(
    private val couponService: CouponService,
    private val customerCouponService: CustomerCouponService
) {
    // 매일 자정에 Coupon 만료일 확인 후 CustomerCoupon 상태 업데이트
    @Scheduled(cron = "0 0 0 * * *")
    fun expireCoupons() {
        val expiredCoupons = couponService.getExpiredCoupons()
        customerCouponService.updateAsExpired(expiredCoupons)
        couponService.deleteCouponKeys(expiredCoupons.map { it.id.toString() })
    }
}
