package kr.hhplus.be.server.support.lock

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class RedisPubSubLockTemplate(
    private val redissonClient: RedissonClient
) : LockTemplate {
    private val locks = ConcurrentHashMap<String, RLock>()

    override fun lock(key: String, waitTime: Long, leaseTime: Long, timeUnit: TimeUnit): Boolean {
        val rLock = redissonClient.getLock(key)
        val acquired = rLock.tryLock(waitTime, leaseTime, timeUnit)
        if (acquired) {
            locks[key] = rLock
        }
        return acquired
    }

    override fun unlock(key: String) {
        val lock = locks[key]
        if (lock != null && lock.isHeldByCurrentThread) {
            try {
                lock.unlock()
            } finally {
                locks.remove(key)
            }
        }
    }
}
