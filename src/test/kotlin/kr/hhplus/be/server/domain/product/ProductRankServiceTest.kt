package kr.hhplus.be.server.domain.product

import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.infrastructure.redis.RedisSortedSetRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProductRankServiceTest {
    private val redisSortedSetRepository = mockk<RedisSortedSetRepository>(relaxed = true)
    private val productRankService = ProductRankService(redisSortedSetRepository)

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
