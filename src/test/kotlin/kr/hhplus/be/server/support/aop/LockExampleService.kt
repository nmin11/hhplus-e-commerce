package kr.hhplus.be.server.support.aop

import kr.hhplus.be.server.support.lock.DistributedLock
import kr.hhplus.be.server.support.lock.LockType
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LockExampleService {
    @DistributedLock(resourceName = "id", key = "#id", lockType = LockType.SPIN)
    fun runSpin(id: Long): String {
        return "spin-$id"
    }

    @DistributedLock(resourceName = "id", key = "#id", lockType = LockType.PUBSUB)
    fun runPubSub(id: Long): String {
        return "pubsub-$id"
    }

    @DistributedLock(
        resourceName = "id",
        key = "#id",
        lockType = LockType.SPIN,
        waitTime = 150,
        leaseTime = 3_000,
        timeUnit = TimeUnit.MILLISECONDS
    )
    fun runSpinWithRestrictedWaitTime(id: Long): String {
        return "spin-$id"
    }
}
