package kr.hhplus.be.server.support.aop

import io.lettuce.core.RedisConnectionException
import kr.hhplus.be.server.support.exception.common.DistributedLockAcquisitionException
import kr.hhplus.be.server.support.lock.LockTemplateRouter
import kr.hhplus.be.server.support.spel.CustomSpringELParser
import kr.hhplus.be.server.support.transaction.RequireNewTransactionExecutor
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Aspect
@Order(AopOrder.DISTRIBUTED_ROCK)
@Component
class DistributedLockAspect(
    private val lockTemplateRouter: LockTemplateRouter,
    private val requireNewTransactionExecutor: RequireNewTransactionExecutor
) {
    private val log = LoggerFactory.getLogger(DistributedLockAspect::class.java)

    @Around("@annotation(distributedLock)")
    fun lock(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val dynamicKey = CustomSpringELParser.getDynamicValue(
            signature.parameterNames,
            joinPoint.args,
            distributedLock.key,
            String::class.java
        )

        val lockKey = buildLockKey(distributedLock.resourceName, dynamicKey)
        val lockTemplate = lockTemplateRouter.route(distributedLock.lockType) // Spin Lock or Pub/Sub Lock 구현체

        var acquired = false
        return try {
            acquired = lockTemplate.lock(
                lockKey,
                distributedLock.waitTime,
                distributedLock.leaseTime,
                distributedLock.timeUnit
            )

            when {
                acquired -> {
                    log.info("\uD83D\uDD12 Lock acquired for key: {}", lockKey)
                    requireNewTransactionExecutor.proceed(joinPoint)
                }
                else -> {
                    log.info("❌ Failed to acquire lock for key: {}", lockKey)
                    throw DistributedLockAcquisitionException(lockKey)
                }
            }
        } catch (e: InterruptedException) {
            throw e
        } catch (e: RedisConnectionException) {
            when {
                distributedLock.fallbackToDatabaseLock -> {
                    log.warn("❗ Redis 연결 실패 → DB Lock fallback 실행")
                    return requireNewTransactionExecutor.proceed(joinPoint)
                }
                else -> throw  e
            }
        } finally {
            try {
                if (acquired) {
                    lockTemplate.unlock(lockKey)
                    log.info("\uD83D\uDD13 Lock released for key: {}", lockKey)
                }
            } catch (_: IllegalMonitorStateException) {
                log.info(
                    "Lock Already Unlocked - serviceName: {}, key: {}",
                    method.name,
                    lockKey
                )
            }
        }
    }

    private fun buildLockKey(resourceName: String, dynamicKey: String): String {
        return "LOCK:$resourceName:$dynamicKey"
    }
}
