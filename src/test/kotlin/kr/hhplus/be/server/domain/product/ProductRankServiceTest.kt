package kr.hhplus.be.server.domain.product

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.infrastructure.redis.RedisSortedSetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProductRankServiceTest {
    private val statisticService = mockk<StatisticService>()
    private val redisRepository = mockk<RedisRepository>(relaxed = true)
    private val redisSortedSetRepository = mockk<RedisSortedSetRepository>(relaxed = true)
    private val productRankService = ProductRankService(
        statisticService,
        redisRepository,
        redisSortedSetRepository
    )

    @Nested
    inner class GetProductRanks {
        @Test
        @DisplayName("캐시 히트 시 캐시에서 인기 상품 랭킹을 조회")
        fun getProductRanks_shouldReturnFromRedisIfCacheHit() {
            // given
            val today = LocalDate.now()
            val periodKey = "3d"
            val redisKey = "product:rank:$periodKey:${today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"

            every { redisRepository.exists(redisKey) } returns true
            every { redisSortedSetRepository.getTopNWithScores(redisKey, 5) } returns listOf(
                "101" to 10.0, "102" to 5.0
            )

            // when
            val result = productRankService.getProductRanks(today.minusDays(3), periodKey)

            // then
            assertThat(result).hasSize(2)
            assertThat(result[0].productId).isEqualTo(101)
            verify(exactly = 1) { redisSortedSetRepository.getTopNWithScores(redisKey, 5) }
            verify(exactly = 0) { statisticService.getTop5PopularProductStatistics(any()) }
        }

        @Test
        @DisplayName("캐시 미스 시 ZSet unionAndStore 호출")
        fun getProductRanks_shouldCallUnionWithCorrectKeys() {
            // given
            val today = LocalDate.now()
            val since = today.minusDays(3)
            val periodKey = "3d"
            val redisKey = "product:rank:$periodKey:${today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"

            every { redisRepository.exists(redisKey) } returns false
            every { redisSortedSetRepository.getTopNWithScores(redisKey, 5) } returns emptyList()

            val expectedKeys = listOf(
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(3),
            ).map {
                "product:sales:${it.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
            }

            // when
            productRankService.getProductRanks(since, periodKey)

            // then
            verify {
                redisSortedSetRepository.unionAndStore(
                    expectedKeys,
                    redisKey,
                    match { it.toHours() < 24 }
                )
            }
        }

        @Test
        @DisplayName("7일 초과 요청 시 DB 조회 후 Redis 저장")
        fun getProductRanks_shouldQueryDb_whenPeriodExceeds7Days() {
            // given
            val today = LocalDate.now()
            val since = today.minusDays(10)
            val periodKey = "10d"
            val redisKey = "product:rank:$periodKey:${today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"

            every { redisRepository.exists(redisKey) } returns false
            every { statisticService.getTop5PopularProductStatistics(since) } returns listOf(
                ProductInfo.Popular(301L, "청바지", 39000, 100)
            )
            every { redisSortedSetRepository.add(any(), any(), any(), any()) } just Runs

            // when
            val result = productRankService.getProductRanks(since, periodKey)

            // then
            assertThat(result).hasSize(1)
            verify { statisticService.getTop5PopularProductStatistics(since) }
            verify {
                redisSortedSetRepository.add(
                    eq(redisKey),
                    eq("301"),
                    eq(100.0),
                    any()
                )
            }
        }
    }

    @Nested
    inner class RefreshRank {
        @Test
        @DisplayName("3일/7일 랭킹 생성 시 ZSet union이 정확히 호출됨")
        fun generateRank_shouldUnionZSetsCorrectly() {
            // given
            val baseDate = LocalDate.of(2025, 5, 14)
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

            val expected3dDstKey = "product:rank:3d:${baseDate.format(formatter)}"
            val expected1wDstKey = "product:rank:1w:${baseDate.format(formatter)}"

            val expected3dSrcKeys = listOf(
                "product:sales:20250513",
                "product:sales:20250512",
                "product:sales:20250511"
            )
            val expected1wSrcKeys = listOf(
                "product:sales:20250513",
                "product:sales:20250512",
                "product:sales:20250511",
                "product:sales:20250510",
                "product:sales:20250509",
                "product:sales:20250508",
                "product:sales:20250507"
            )

            // when
            productRankService.refreshRank(baseDate)

            // then
            verify(exactly = 1) {
                redisSortedSetRepository.unionAndStore(expected3dSrcKeys, expected3dDstKey, Duration.ofHours(25))
            }
            verify(exactly = 1) {
                redisSortedSetRepository.unionAndStore(expected1wSrcKeys, expected1wDstKey, Duration.ofHours(25))
            }
        }
    }
}
