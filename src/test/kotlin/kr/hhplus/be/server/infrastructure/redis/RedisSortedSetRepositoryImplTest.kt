package kr.hhplus.be.server.infrastructure.redis

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.time.Duration

class RedisSortedSetRepositoryImplTest {
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var redisZSetRepository: RedisSortedSetRepositoryImpl

    @BeforeEach
    fun setup() {
        redisTemplate = mockk()
        redisZSetRepository = RedisSortedSetRepositoryImpl(redisTemplate)
    }

    @Nested
    inner class GetTopNWithScores {
        @Test
        @DisplayName("ZSet 에서 상위 N개의 멤버와 점수를 정렬된 순서로 반환")
        fun returnTopNMembersWithScores() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops

            val tuple1 = mockk<ZSetOperations.TypedTuple<String>>()
            every { tuple1.value } returns "product1"
            every { tuple1.score } returns 10.0

            val tuple2 = mockk<ZSetOperations.TypedTuple<String>>()
            every { tuple2.value } returns "product2"
            every { tuple2.score } returns 8.5

            every { ops.reverseRangeWithScores("zSetKey", 0, 4) } returns setOf(tuple1, tuple2)

            // when
            val result = redisZSetRepository.getTopNWithScores("zSetKey", 5)

            // then
            assertThat(result).containsExactly(
                "product1" to 10.0,
                "product2" to 8.5
            )
        }

        @Test
        @DisplayName("ZSet의 멤버 중 값 또는 점수가 null인 항목은 제외")
        fun skipNullValuesOrScores() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops

            val validTuple = mockk<ZSetOperations.TypedTuple<String>>()
            every { validTuple.value } returns "product1"
            every { validTuple.score } returns 5.0

            val nullValueTuple = mockk<ZSetOperations.TypedTuple<String>>()
            every { nullValueTuple.value } returns null
            every { nullValueTuple.score } returns 3.0

            val nullScoreTuple = mockk<ZSetOperations.TypedTuple<String>>()
            every { nullScoreTuple.value } returns "product2"
            every { nullScoreTuple.score } returns null

            every {
                ops.reverseRangeWithScores("zSetKey", 0, 2)
            } returns setOf(validTuple, nullValueTuple, nullScoreTuple)

            // when
            val result = redisZSetRepository.getTopNWithScores("zSetKey", 3)

            // then
            assertThat(result).containsExactly("product1" to 5.0)
        }

        @Test
        @DisplayName("ZSet에 값이 없을 경우 빈 리스트 반환")
        fun returnEmptyListWhenNoData() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.reverseRangeWithScores("emptyKey", 0, 4) } returns null

            // when
            val result = redisZSetRepository.getTopNWithScores("emptyKey", 5)

            // then
            assertThat(result).isEmpty()
        }
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
            redisZSetRepository.add("zSetKey", "productId", 10.0, Duration.ofHours(1))

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
            redisZSetRepository.add("zSetKey", "productId", 10.0, null)

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
            redisZSetRepository.incrementScore("zSetKey", "productId", 5.0)

            // then
            verify { ops.incrementScore("zSetKey", "productId", 5.0) }
        }
    }

    @Nested
    inner class UnionAndStore {
        @Test
        @DisplayName("여러 ZSet을 병합하여 새로운 키로 저장하고 TTL 설정")
        fun unionMultipleZSetsWithTtl() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.unionAndStore("key1", listOf("key2", "key3"), "dstKey") } returns 3L
            every { redisTemplate.expire("dstKey", Duration.ofMinutes(30)) } returns true

            val sourceKeys = listOf("key1", "key2", "key3")
            val destinationKey = "dstKey"
            val ttl = Duration.ofMinutes(30)

            // when
            redisZSetRepository.unionAndStore(sourceKeys, destinationKey, ttl)

            // then
            verify { ops.unionAndStore("key1", listOf("key2", "key3"), "dstKey") }
            verify { redisTemplate.expire("dstKey", ttl) }
        }

        @Test
        @DisplayName("TTL이 null일 경우 expire를 호출하지 않음")
        fun unionZSetsWithoutTtl() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.unionAndStore("key1", listOf("key2"), "dstKey") } returns 2L

            val sourceKeys = listOf("key1", "key2")
            val destinationKey = "dstKey"
            val ttl: Duration? = null

            // when
            redisZSetRepository.unionAndStore(sourceKeys, destinationKey, ttl)

            // then
            verify { ops.unionAndStore("key1", listOf("key2"), "dstKey") }
            verify(exactly = 0) { redisTemplate.expire(any(), any()) }
        }
    }
}
