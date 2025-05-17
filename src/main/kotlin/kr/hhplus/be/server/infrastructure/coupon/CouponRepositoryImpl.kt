package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.infrastructure.redis.lua.LuaScriptId
import kr.hhplus.be.server.infrastructure.redis.lua.LuaScriptRegistry
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class CouponRepositoryImpl(
    private val couponJpaRepository: CouponJpaRepository,
    private val stringRedisTemplate: StringRedisTemplate,
    private val luaScriptRegistry: LuaScriptRegistry
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

    override fun findByIdWithLock(id: Long): Coupon? {
        return couponJpaRepository.findByIdWithLock(id)
    }

    override fun issue(coupon: Coupon, customerId: Long): CouponIssueResult {
        val script = luaScriptRegistry.getScript(LuaScriptId.COUPON_ISSUE, Long::class.java)
        val now = LocalDateTime.now()
        val expireAt = coupon.expiredAt.atStartOfDay()
        val ttl = Duration.between(now, expireAt).coerceAtLeast(Duration.ZERO)

        val result = stringRedisTemplate.execute(
            script,
            listOf(
                "coupon:stock:${coupon.id}",
                "coupon:issued:${coupon.id}"
            ),
            customerId.toString(),
            ttl.seconds.toString()
        )

        return CouponIssueResult.fromCode(result)
    }
}
