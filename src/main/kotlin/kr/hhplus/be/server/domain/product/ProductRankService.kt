package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infrastructure.redis.RedisSortedSetRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ProductRankService(
    private val redisSortedSetRepository: RedisSortedSetRepository
) {
    companion object {
        private const val DST_KEY_PATTERN = "product:rank:%s:%s"
        private const val SRC_KEY_PATTERN = "product:sales:%s"
        private val TTL = Duration.ofHours(25)
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    fun refreshRank(baseDate: LocalDate) {
        val threeDaysRankDstKey = DST_KEY_PATTERN.format("3d", dateFormatter.format(baseDate))
        val sevenDaysRankDstKey = DST_KEY_PATTERN.format("1w", dateFormatter.format(baseDate))

        val threeDaysRankSrcKeys = (1L .. 3L).map {
            SRC_KEY_PATTERN.format(
                baseDate.minusDays(it).format(dateFormatter)
            )
        }
        val sevenDaysRankSrcKeys = (1L .. 7L).map {
            SRC_KEY_PATTERN.format(
                baseDate.minusDays(it).format(dateFormatter)
            )
        }

        redisSortedSetRepository.unionAndStore(threeDaysRankSrcKeys, threeDaysRankDstKey, TTL)
        redisSortedSetRepository.unionAndStore(sevenDaysRankSrcKeys, sevenDaysRankDstKey, TTL)
    }
}
