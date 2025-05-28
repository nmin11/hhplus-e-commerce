package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.infrastructure.coupon.CouponIssueResult
import kr.hhplus.be.server.support.exception.coupon.CouponInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CouponInvalidPeriodException
import kr.hhplus.be.server.support.exception.coupon.CouponIssueFailedException
import kr.hhplus.be.server.support.exception.coupon.CouponNotFoundException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyIssuedException
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

    fun getExpiredCoupons(referenceDate: LocalDate = LocalDate.now()): List<Coupon> {
        return couponRepository.findAllByExpiredAtBefore(referenceDate)
    }

    @Transactional
    fun decreaseQuantity(coupon: Coupon, quantity: Int) {
        coupon.decreaseQuantity(quantity)
        couponRepository.save(coupon)
    }

    fun issue(couponId: Long, customerId: Long) {
        val result = couponRepository.issue(couponId, customerId)

        when (result) {
            CouponIssueResult.SUCCESS -> return
            CouponIssueResult.NON_FOUND -> throw CouponNotFoundException()
            CouponIssueResult.ALREADY_ISSUED -> throw CustomerCouponAlreadyIssuedException()
            CouponIssueResult.INSUFFICIENT -> throw CouponInsufficientException()
            CouponIssueResult.EXPIRED -> throw CouponInvalidPeriodException()
            else -> throw CouponIssueFailedException()
        }
    }

    fun deleteCouponKeys(couponIds: List<String>) {
        couponIds.map { "coupon:issued:${it}" }
            .forEach { key -> couponRepository.deleteKey(key) }
    }
}
