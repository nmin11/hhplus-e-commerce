package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.infrastructure.coupon.CouponIssueResult
import java.time.LocalDate

interface CouponRepository {
    fun save(coupon: Coupon): Coupon
    fun findById(id: Long): Coupon?
    fun findAllByExpiredAtBefore(expiredAt: LocalDate): List<Coupon>
    fun findByIdWithLock(id: Long): Coupon?
    fun issue(couponId: Long, customerId: Long): CouponIssueResult
    fun deleteKey(couponKey: String)
}
