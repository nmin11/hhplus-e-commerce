package kr.hhplus.be.server.support.lock

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
class RedisPubSubLockTemplateTest {
    @MockK
    private lateinit var redissonClient: RedissonClient

    @MockK
    private lateinit var rLock: RLock

    private lateinit var lockTemplate: RedisPubSubLockTemplate

    @BeforeEach
    fun setUp() {
        lockTemplate = RedisPubSubLockTemplate(redissonClient)
        every { redissonClient.getLock(any<String>()) } returns rLock
    }

    @Test
    @DisplayName("락을 정상적으로 획득하고 true 반환")
    fun returnsTrueWhenLockAcquired() {
        // given
        every { rLock.tryLock(any(), any(), any()) } returns true

        // when
        val result = lockTemplate.lock("LOCK:test", 1000, 3000, TimeUnit.MILLISECONDS)

        // then
        assertThat(result).isTrue()
        verify { rLock.tryLock(1000, 3000, TimeUnit.MILLISECONDS) }
    }

    @Test
    @DisplayName("락을 획득하지 못하면 false 반환")
    fun returnsFalseWhenLockFails() {
        // given
        every { rLock.tryLock(any(), any(), any()) } returns false

        // when
        val result = lockTemplate.lock("LOCK:test", 1000, 3000, TimeUnit.MILLISECONDS)

        // then
        assertThat(result).isFalse()
        verify { rLock.tryLock(1000, 3000, TimeUnit.MILLISECONDS) }
    }

    @Test
    @DisplayName("락을 보유한 스레드가 unlock 호출 시 성공적으로 해제됨")
    fun unlocksSuccessfullyWhenHeldByCurrentThread() {
        // given
        every { rLock.tryLock(any(), any(), any()) } returns true
        every { rLock.isHeldByCurrentThread } returns true
        every { rLock.unlock() } just Runs

        // when
        lockTemplate.lock("LOCK:test", 1000, 3000, TimeUnit.MILLISECONDS)
        lockTemplate.unlock("LOCK:test")

        // then
        verify { rLock.unlock() }
    }

    @Test
    @DisplayName("락을 보유하지 않은 상태에서 unlock 호출 시 아무 일도 발생하지 않음")
    fun unlockIgnoredWhenNotHeldByCurrentThread() {
        // given
        every { rLock.tryLock(any(), any(), any()) } returns true
        every { rLock.isHeldByCurrentThread } returns false

        // when
        lockTemplate.lock("LOCK:test", 1000, 3000, TimeUnit.MILLISECONDS)
        lockTemplate.unlock("LOCK:test")

        // then
        verify(exactly = 0) { rLock.unlock() }
    }
}
