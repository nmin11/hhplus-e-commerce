package kr.hhplus.be.server.support.lock

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
class RedisSpinLockTemplateTest {
    @MockK
    private lateinit var redisRepository: RedisRepository

    private lateinit var lockTemplate: RedisSpinLockTemplate

    @BeforeEach
    fun setUp() {
        lockTemplate = RedisSpinLockTemplate(redisRepository)
    }

    @Test
    @DisplayName("락을 첫 시도에 획득하고 true 반환")
    fun returnsTrueWhenLockAcquiredImmediately() {
        // given
        every { redisRepository.setIfAbsent(any(), any(), any()) } returns true

        // when
        val result = lockTemplate.lock("LOCK:test", 1000, 3000, TimeUnit.MILLISECONDS)

        // then
        assertThat(result).isTrue()
        verify(exactly = 1) { redisRepository.setIfAbsent(any(), any(), any()) }
    }

    @Test
    @DisplayName("재시도 끝에도 락을 획득하지 못하면 false 반환")
    fun returnsFalseWhenLockFailsAfterRetries() {
        // given
        every { redisRepository.setIfAbsent(any(), any(), any()) } returns false

        // when
        val result = lockTemplate.lock("LOCK:test", 300, 1000, TimeUnit.MILLISECONDS)

        // then
        assertThat(result).isFalse()
        verify(exactly = 3) { redisRepository.setIfAbsent(any(), any(), any()) }
    }

    @Test
    @DisplayName("락 해제 시 LuaScript 방식의 release 호출")
    fun callsLuaReleaseOnUnlock() {
        // given
        every { redisRepository.setIfAbsent(any(), any(), any()) } returns true
        every { redisRepository.releaseWithLua(any(), any()) } returns

        // when
        lockTemplate.lock("LOCK:test", 1000, 1000, TimeUnit.MILLISECONDS)
        lockTemplate.unlock("LOCK:test")

        // then
        verify { redisRepository.releaseWithLua(match { it.startsWith("LOCK:") }, any()) }
    }
}
