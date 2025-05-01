package kr.hhplus.be.server.support.aop

import com.ninjasquad.springmockk.SpykBean
import io.lettuce.core.RedisConnectionException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.support.lock.LockTemplateRouter
import kr.hhplus.be.server.support.lock.LockType
import kr.hhplus.be.server.support.lock.RedisPubSubLockTemplate
import kr.hhplus.be.server.support.lock.RedisSpinLockTemplate
import kr.hhplus.be.server.support.transaction.RequireNewTransactionExecutor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
class DistributedLockAspectIntegrationTest {
    @Autowired
    lateinit var lockExampleService: LockExampleService

    @SpykBean
    lateinit var lockTemplateRouter: LockTemplateRouter

    @SpykBean
    lateinit var spinLockTemplate: RedisSpinLockTemplate

    @SpykBean
    lateinit var pubSubLockTemplate: RedisPubSubLockTemplate

    @SpykBean
    lateinit var requireNewTransactionExecutor: RequireNewTransactionExecutor

    @SpykBean
    lateinit var redisRepository: RedisRepository

    @SpykBean
    lateinit var redissonClient: RedissonClient

    @BeforeEach
    fun reset() {
        clearAllMocks()
    }

    @Test
    @DisplayName("Spin Lock 정상 작동")
    fun spinLock_shouldSuccess() {
        // given
        val lockId = 1L
        val lockKey = "LOCK:id:$lockId"

        // when
        val result = lockExampleService.runSpin(lockId)

        // then
        assertThat(result).isEqualTo("spin-$lockId")
        verifyOrder {
            lockTemplateRouter.route(LockType.SPIN)
            spinLockTemplate.lock(lockKey, 3L, 3L, TimeUnit.SECONDS)
            requireNewTransactionExecutor.proceed(any())
            spinLockTemplate.unlock(lockKey)
        }
    }

    @Test
    @DisplayName("Pub/Sub Lock 정상 작동")
    fun pubSubLock_shouldSuccess() {
        // given
        val lockId = 2L
        val lockKey = "LOCK:id:$lockId"

        // when
        val result = lockExampleService.runPubSub(lockId)

        // then
        assertThat(result).isEqualTo("pubsub-$lockId")
        verifyOrder {
            lockTemplateRouter.route(LockType.PUBSUB)
            pubSubLockTemplate.lock(lockKey, 3L, 3L, TimeUnit.SECONDS)
            requireNewTransactionExecutor.proceed(any())
            pubSubLockTemplate.unlock(lockKey)
        }
    }

    @Test
    @DisplayName("Spin Lock 재시도 이후 락 획득")
    fun spinLock_shouldRetryAndSuccess() {
        // given
        val lockId = 3L
        val lockKey = "LOCK:id:$lockId"

        // 2회 시도 이후 성공
        every { redisRepository.setIfAbsent(any(), any(), any()) } returnsMany listOf(false, false, true)

        // when
        val result = lockExampleService.runSpin(lockId)

        // then
        assertThat(result).isEqualTo("spin-$lockId")
        verifyOrder {
            lockTemplateRouter.route(LockType.SPIN)
            spinLockTemplate.lock(lockKey, 3L, 3L, TimeUnit.SECONDS)
            redisRepository.setIfAbsent(lockKey, any(), any())
            redisRepository.setIfAbsent(lockKey, any(), any())
            redisRepository.setIfAbsent(lockKey, any(), any())
            requireNewTransactionExecutor.proceed(any())
            spinLockTemplate.unlock(lockKey)
        }
    }

    @Test
    @DisplayName("Spin Lock 재시도 중 waitTIme 도달 시 예외 발생")
    fun spinLock_shouldFailAfterMaxRetries() {
        // given
        val lockId = 4L
        val lockKey = "LOCK:id:$lockId"

        // 재시도를 몇 번 해도 Lock 획득에 실패
        every { redisRepository.setIfAbsent(lockKey, any(), any()) } returns false

        // when
        val exception = assertThrows<IllegalStateException> {
            lockExampleService.runSpinWithRestrictedWaitTime(lockId)
        }

        // then
        assertThat(exception.message).contains("Failed to acquire lock")
        verifyOrder {
            lockTemplateRouter.route(LockType.SPIN)
            spinLockTemplate.lock(lockKey, 150L, 3_000L, TimeUnit.MILLISECONDS)
            redisRepository.setIfAbsent(lockKey, any(), any())
            redisRepository.setIfAbsent(lockKey, any(), any())
        }
        verify(exactly = 0) { spinLockTemplate.unlock(lockKey) }
        verify(exactly = 0) { redisRepository.releaseWithLua(lockKey, any()) }
        verify(exactly = 0) { requireNewTransactionExecutor.proceed(any()) }
        verify(exactly = 2) { redisRepository.setIfAbsent(lockKey, any(), any()) } // 정확히 2번의 락 획득 시도
    }

    @Test
    @DisplayName("Pub/Sub Lock 획득 실패 시 예외 발생")
    fun pubSubLock_shouldFailToAcquire() {
        // given
        val lockId = 5L
        val lockKey = "LOCK:id:$lockId"
        every { redissonClient.getLock(lockKey).tryLock(any(), any(), any()) } returns false

        // when
        val exception = assertThrows<IllegalStateException> {
            lockExampleService.runPubSub(lockId)
        }

        // then
        assertThat(exception.message).contains("Failed to acquire lock")
        verify(exactly = 0) { pubSubLockTemplate.unlock(lockKey) }
    }

    @Test
    @DisplayName("Pub/Sub Lock 획득 중 InterruptedException 발생")
    fun pubSubLock_shouldThrowInterrupted() {
        // given
        val lockId = 6L
        val lockKey = "LOCK:id:$lockId"
        every { redissonClient.getLock(lockKey).tryLock(any(), any(), any()) } throws InterruptedException()

        // when & then
        assertThrows<InterruptedException> {
            lockExampleService.runPubSub(lockId)
        }
    }

    @Test
    @DisplayName("Pub/Sub Lock 해제 시 락이 이미 해제된 상태여도 무시")
    fun pubSubLock_shouldIgnoreIllegalMonitorStateException() {
        val lockId = 7L
        val lockKey = "LOCK:id:$lockId"

        val rLock = mockk<RLock>()
        every { redissonClient.getLock(lockKey) } returns rLock
        every { rLock.tryLock(any(), any(), any()) } returns true
        every { rLock.isHeldByCurrentThread } returns true
        every { rLock.unlock() } throws IllegalMonitorStateException()

        // when
        val result = lockExampleService.runPubSub(lockId)

        // then
        assertThat(result).isEqualTo("pubsub-7")
        verify { rLock.unlock() }
    }

    @Test
    @DisplayName("Redis 장애 발생 시 fallback 설정이 되어 있다면 DB 락 로직을 실행")
    fun fallbackToDbLock_whenRedisUnavailable_shouldProceedWithDbLock() {
        // given
        val lockId = 8L
        val lockKey = "LOCK:id:$lockId"

        // Redis 락 시도 중 장애 발생
        every {
            redissonClient.getLock(lockKey).tryLock(any(), any(), any())
        } throws RedisConnectionException("Redis not reachable")

        // when
        val result = lockExampleService.runPubSubWithFallback(lockId)

        // then
        assertThat(result).isEqualTo("pubsub-fallback-$lockId")
        verify(exactly = 1) { requireNewTransactionExecutor.proceed(any()) }
    }
}
