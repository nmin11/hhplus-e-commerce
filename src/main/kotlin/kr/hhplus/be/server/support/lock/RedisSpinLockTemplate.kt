package kr.hhplus.be.server.support.lock

import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component
class RedisSpinLockTemplate(
    private val redisRepository: RedisRepository
) : LockTemplate {
    private val lockValue = ThreadLocal.withInitial { UUID.randomUUID().toString() }

    override fun lock(key: String, waitTime: Long, leaseTime: Long, timeUnit: TimeUnit): Boolean {
        val value = lockValue.get()
        val expire = Duration.ofMillis(timeUnit.toMillis(leaseTime))
        val timeoutAt = System.currentTimeMillis() + timeUnit.toMillis(waitTime)
        val sleepMillis = 100L // 100ms 단위로 재시도

        while (System.currentTimeMillis() < timeoutAt) {
            val acquired = redisRepository.setIfAbsent(key, value, expire)
            if (acquired) return true
            Thread.sleep(sleepMillis)
        }

        return false
    }

    override fun unlock(key: String) {
        redisRepository.releaseWithLua(key, lockValue.get())
    }
}
