package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.infrastructure.redis.lua.LuaScriptId
import kr.hhplus.be.server.infrastructure.redis.lua.LuaScriptRegistry
import kr.hhplus.be.server.support.exception.coupon.CouponInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CouponIssueFailedException
import kr.hhplus.be.server.support.exception.coupon.CouponNotFoundException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyIssuedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val redisRepository: RedisRepository,
    private val luaScriptRegistry: LuaScriptRegistry
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
    fun decreaseQuantity(coupon: Coupon) {
        coupon.decreaseQuantity()
    }

    fun issueWithRedis(coupon: Coupon, customerId: Long) {
        val script = luaScriptRegistry.getScript(LuaScriptId.COUPON_ISSUE, Long::class.java)
        val now = LocalDateTime.now()
        val expireAt = coupon.expiredAt.atStartOfDay()
        val ttl = Duration.between(now, expireAt).coerceAtLeast(Duration.ZERO)

        val resultCode = redisRepository.executeWithLua(
            script,
            keys = listOf(
                "coupon:stock:${coupon.id}",
                "coupon:issued:${coupon.id}"
            ),
            args = listOf(
                customerId.toString(),
                ttl.seconds.toString()
            )
        )

        val result = CouponInfo.IssueResult.fromCode(resultCode)

        when (result) {
            CouponInfo.IssueResult.SUCCESS -> return
            CouponInfo.IssueResult.NON_FOUND -> throw CouponNotFoundException()
            CouponInfo.IssueResult.ALREADY_ISSUED -> throw CustomerCouponAlreadyIssuedException()
            CouponInfo.IssueResult.INSUFFICIENT -> throw CouponInsufficientException()
            else -> throw CouponIssueFailedException()
        }
    }
}
