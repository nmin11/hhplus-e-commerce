package kr.hhplus.be.server.infrastructure.product

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

class ProductRankRepositoryImplTest {
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var productRankRepository: ProductRankRepositoryImpl

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        productRankRepository = ProductRankRepositoryImpl(redisTemplate)
    }

    @Nested
    inner class ExistsRankKey {
        @Test
        @DisplayName("지정한 키가 존재할 경우 true 반환")
        fun shouldReturnTrueWhenKeyExists() {
            // given
            every { redisTemplate.hasKey("product:rank:3d:20250515") } returns true

            // when
            val result = productRankRepository.existsRankKey("product:rank:3d:20250515")

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class GetTopNWithSalesCount {
        @Test
        @DisplayName("ZSet 에서 top N 데이터를 조회해 ProductRankRedisEntry로 반환")
        fun shouldReturnSortedEntries() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops

            val tuple = mockk<ZSetOperations.TypedTuple<String>>()
            every { tuple.value } returns "101"
            every { tuple.score } returns 50.0

            every { ops.reverseRangeWithScores("product:rank:3d:20250515", 0, 4) } returns setOf(tuple)

            // when
            val result = productRankRepository.getTopNWithSalesCount("product:rank:3d:20250515", 5)

            // then
            assertThat(result).containsExactly(ProductRankRedisEntry(101L, 50))
        }
    }

    @Nested
    @DisplayName("addRankEntry")
    inner class AddRankEntry {
        @Test
        @DisplayName("ZSet에 랭킹 엔트리를 추가하고 TTL 설정")
        fun shouldAddRankEntryWithTtl() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.add(any(), any(), any()) } returns true
            every { redisTemplate.expire(any(), any()) } returns true

            val entry = ProductRankRedisEntry(101L, 50)

            // when
            productRankRepository.addRankEntry("product:rank:3d:20250515", entry, Duration.ofHours(1))

            // then
            verify {
                ops.add("product:rank:3d:20250515", "101", 50.0)
                redisTemplate.expire("product:rank:3d:20250515", Duration.ofHours(1))
            }
        }
    }

    @Nested
    @DisplayName("incrementProductSales")
    inner class IncrementProductSales {
        @Test
        @DisplayName("ZSet 멤버의 점수를 증가시킴")
        fun shouldIncrementScore() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.incrementScore("product:sales:20250515", "101", 2.0) } returns 52.0

            // when
            productRankRepository.incrementProductSales("product:sales:20250515", 101L, 2)

            // then
            verify {
                ops.incrementScore("product:sales:20250515", "101", 2.0)
            }
        }
    }

    @Nested
    inner class UnionRanks {
        @Test
        @DisplayName("여러 ZSet을 합쳐서 목적지 키로 저장 후 TTL 설정")
        fun shouldUnionZSetAndExpire() {
            // given
            val ops = mockk<ZSetOperations<String, String>>()
            every { redisTemplate.opsForZSet() } returns ops
            every { ops.unionAndStore("k1", listOf("k2", "k3"), "dst") } returns 5L
            every { redisTemplate.expire("dst", Duration.ofHours(1)) } returns true

            val keys = listOf("k1", "k2", "k3")

            // when
            productRankRepository.unionRanks(keys, "dst", Duration.ofHours(1))

            // then
            verify {
                ops.unionAndStore("k1", listOf("k2", "k3"), "dst")
                redisTemplate.expire("dst", Duration.ofHours(1))
            }
        }
    }
}
