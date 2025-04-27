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

    fun getByIdWithLock(couponId: Long): Coupon {
        return couponRepository.findByIdWithLock(couponId)
            ?: throw IllegalArgumentException("쿠폰 정보가 존재하지 않습니다.")
    }

    fun decreaseQuantity(coupon: Coupon) {
        coupon.decreaseQuantity()
    }

    fun getExpiredCoupons(referenceDate: LocalDate = LocalDate.now()): List<Coupon> {
        return couponRepository.findAllByExpiredAtBefore(referenceDate)
    }
}
