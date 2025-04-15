package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface CouponJpaRepository : JpaRepository<Coupon, Long> {
    fun save(coupon: Coupon): Coupon
    fun findAllByExpiredAtBefore(data: LocalDate): List<Coupon>
}
