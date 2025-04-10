package kr.hhplus.be.server.domain.coupon

import java.time.LocalDate

interface CouponRepository {
    fun save(coupon: Coupon): Coupon
    fun findById(id: Long): Coupon?
    fun findAllByExpiredAtBefore(data: LocalDate): List<Coupon>
}
