package kr.hhplus.be.server.support.lock

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val resourceName: String,
    val lockType: LockType,
    val waitTime: Long = 3L,
    val leaseTime: Long = 3L,
    val timeUnit: TimeUnit = TimeUnit.SECONDS
)
