package kr.hhplus.be.server.infrastructure.coupon

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.domain.coupon.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface CouponJpaRepository : JpaRepository<Coupon, Long> {
    fun save(coupon: Coupon): Coupon
    fun findAllByExpiredAtBefore(data: LocalDate): List<Coupon>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    fun findWithLockById(id: Long): Coupon
}
