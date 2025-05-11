package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.support.exception.coupon.CouponNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CouponService(
    private val couponRepository: CouponRepository
) {
    fun getById(couponId: Long): Coupon {
        return couponRepository.findById(couponId)
            ?: throw CouponNotFoundException()
    }

    fun getByIdWithLock(couponId: Long): Coupon {
        return couponRepository.findByIdWithLock(couponId)
            ?: throw CouponNotFoundException()
    }

    @Transactional
    fun decreaseQuantity(coupon: Coupon) {
        coupon.decreaseQuantity()
    }

    fun getExpiredCoupons(referenceDate: LocalDate = LocalDate.now()): List<Coupon> {
        return couponRepository.findAllByExpiredAtBefore(referenceDate)
    }
}
