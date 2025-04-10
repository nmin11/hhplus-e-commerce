package kr.hhplus.be.server.domain.coupon

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CouponService(
    private val couponRepository: CouponRepository
) {
    fun getById(couponId: Long): Coupon {
        return couponRepository.findById(couponId)
            ?: throw IllegalArgumentException("쿠폰 정보가 존재하지 않습니다.")
    }

    fun calculateDiscount(coupon: Coupon, totalPrice: Int): Int {
        val now = LocalDate.now()
        if (now.isBefore(coupon.startedAt) || now.isAfter(coupon.expiredAt)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다.")
        }

        return when (coupon.discountType) {
            DiscountType.FIXED -> coupon.discountAmount
            DiscountType.RATE -> (totalPrice * coupon.discountAmount / 100.0).toInt()
        }
    }

    fun decreaseQuantity(coupon: Coupon) {
        if (coupon.currentQuantity <= 0) {
            throw IllegalStateException("쿠폰 수량이 모두 소진되었습니다.")
        }

        coupon.currentQuantity -= 1
        couponRepository.save(coupon)
    }
}
