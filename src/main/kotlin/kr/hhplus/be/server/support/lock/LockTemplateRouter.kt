package kr.hhplus.be.server.support.lock

import org.springframework.stereotype.Component

@Component
class LockTemplateRouter(
    private val spinLockTemplate: RedisSpinLockTemplate,
    private val pubSubLockTemplate: RedisPubSubLockTemplate
) {
    fun route(lockType: LockType): LockTemplate {
        return when (lockType) {
            LockType.SPIN -> spinLockTemplate
            LockType.PUBSUB -> pubSubLockTemplate
        }
    }
}
