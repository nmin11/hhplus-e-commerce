package kr.hhplus.be.server.infrastructure.redis

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.time.Duration

class RedisSortedSetRepositoryImplTest {
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var redisZSetRepo: RedisSortedSetRepositoryImpl

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        redisZSetRepo = RedisSortedSetRepositoryImpl(redisTemplate)
    }

    @Nested
    inner class Add {
        @Test
        @DisplayName("ZSet에 멤버를 추가하고 TTL 설정")
        fun addMemberWithTtl() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.add("zSetKey", "productId", 10.0) } returns true
            every { redisTemplate.expire("zSetKey", Duration.ofHours(1)) } returns true

            // when
            redisZSetRepo.add("zSetKey", "productId", 10.0, Duration.ofHours(1))

            // then
            verify { ops.add("zSetKey", "productId", 10.0) }
            verify { redisTemplate.expire("zSetKey", Duration.ofHours(1)) }
        }

        @Test
        @DisplayName("TTL이 null일 경우 expire를 호출하지 않음")
        fun addMemberWithoutTtl() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.add("zSetKey", "productId", 10.0) } returns true

            // when
            redisZSetRepo.add("zSetKey", "productId", 10.0, null)

            // then
            verify { ops.add("zSetKey", "productId", 10.0) }
            verify(exactly = 0) { redisTemplate.expire(any(), any()) }
        }
    }

    @Nested
    inner class IncrementScore {
        @Test
        @DisplayName("ZSet의 멤버 점수를 증가시킴")
        fun incrementScoreOfMember() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.incrementScore("zSetKey", "productId", 5.0) } returns 15.0

            // when
            redisZSetRepo.incrementScore("zSetKey", "productId", 5.0)

            // then
            verify { ops.incrementScore("zSetKey", "productId", 5.0) }
        }
    }
}
