package kr.hhplus.be.server.domain.coupon

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val customerCouponRepository: CustomerCouponRepository
) {
    fun getById(couponId: Long): Coupon {
        return couponRepository.findById(couponId)
            ?: throw IllegalArgumentException("쿠폰 정보가 존재하지 않습니다.")
    }

    fun validateAndGetDiscount(couponId: Long, customerId: Long): Int {
        val customerCoupon = customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
            ?: throw IllegalArgumentException("해당 쿠폰은 고객에게 발급되지 않았습니다.")

        if (customerCoupon.status != CustomerCouponStatus.AVAILABLE) {
            throw IllegalStateException("만료되었거나 사용된 쿠폰입니다.")
        }

        val coupon = getById(couponId)
        val now = LocalDateTime.now()
        if (now.isBefore(coupon.startedAt) || now.isAfter(coupon.expiredAt)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다.")
        }

        return coupon.discountAmount
    }
}
