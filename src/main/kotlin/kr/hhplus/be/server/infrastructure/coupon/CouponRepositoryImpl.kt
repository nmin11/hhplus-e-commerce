package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class CouponRepositoryImpl(
    private val couponJpaRepository: CouponJpaRepository
) : CouponRepository {
    override fun save(coupon: Coupon): Coupon {
        return couponJpaRepository.save(coupon)
    }

    override fun findById(id: Long): Coupon? {
        return couponJpaRepository.findById(id).orElse(null)
    }

    override fun findAllByExpiredAtBefore(expiredAt: LocalDate): List<Coupon> {
        return couponJpaRepository.findAllByExpiredAtBefore(expiredAt)
    }
}
