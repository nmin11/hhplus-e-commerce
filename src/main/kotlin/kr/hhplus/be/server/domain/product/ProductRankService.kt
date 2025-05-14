package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.infrastructure.redis.RedisSortedSetRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class ProductRankService(
    private val statisticService: StatisticService,
    private val redisRepository: RedisRepository,
    private val redisSortedSetRepository: RedisSortedSetRepository
) {
    companion object {
        private const val DST_KEY_PATTERN = "product:rank:%s:%s"
        private const val SRC_KEY_PATTERN = "product:sales:%s"
        private val SCHEDULED_TTL = Duration.ofHours(25)
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    private val log = LoggerFactory.getLogger(ProductRankService::class.java)

    fun getProductRanks(since: LocalDate, periodKey: String): List<ProductInfo.Rank> {
        val today = LocalDate.now()
        log.info("today: ${today.format(dateFormatter)}")
        val redisKey = DST_KEY_PATTERN.format(periodKey, today.format(dateFormatter))
        val betweenDays = ChronoUnit.DAYS.between(since, today)
        log.info("betweenDays: $betweenDays")

        return if (redisRepository.exists(redisKey)) {
            log.info("✅ [Cache Hit]")
            getProductRanksFromRedis(redisKey)

        } else if (betweenDays <= 7) {
            log.info("⚠️ [Cache Miss] → Redis ZSet 랭킹 생성")

            cacheProductRankByUnion(since, today, redisKey, periodKey)
            getProductRanksFromRedis(redisKey)
        } else {
            log.info("⚠️ [Cache Miss] → 기간 범위를 초과하여 DB 조회")
            val ttl = ttlForPeriodKey(periodKey)
            getProductRanksFromDatabase(since, redisKey, ttl)
        }
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

        redisSortedSetRepository.unionAndStore(threeDaysRankSrcKeys, threeDaysRankDstKey, SCHEDULED_TTL)
        redisSortedSetRepository.unionAndStore(sevenDaysRankSrcKeys, sevenDaysRankDstKey, SCHEDULED_TTL)
    }

    private fun ttlForPeriodKey(periodKey: String): Duration {
        return when (periodKey) {
            "1d" -> Duration.ofMinutes(5)
            "3d", "1w" -> ttlUntilMidnight()
            else -> Duration.ofHours(1)
        }
    }

    private fun ttlUntilMidnight(): Duration {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return Duration.between(now, midnight)
    }

    private fun cacheProductRankByUnion(
        since: LocalDate,
        today: LocalDate,
        redisKey: String,
        periodKey: String
    ) {
        val betweenDays = ChronoUnit.DAYS.between(since, today)

        val sourceKeys = (0..betweenDays - 1).map {
            val date = today.minusDays(it)
            SRC_KEY_PATTERN.format(date.format(dateFormatter))
        }

        val ttl = ttlForPeriodKey(periodKey)
        redisSortedSetRepository.unionAndStore(sourceKeys, redisKey, ttl)
    }

    private fun getProductRanksFromRedis(redisKey: String): List<ProductInfo.Rank> {
        return redisSortedSetRepository.getTopNWithScores(redisKey, 5).map { (value, score) ->
            val productId = value.toLong()
            ProductInfo.Rank(productId, score.toInt())
        }
    }

    private fun getProductRanksFromDatabase(
        since: LocalDate,
        redisKey: String,
        ttl: Duration
    ): List<ProductInfo.Rank> {
        val result = statisticService.getTop5PopularProductStatistics(since)
            .map { ProductInfo.Rank(it.id, it.totalSales) }

        result.forEach {
            redisSortedSetRepository.add(redisKey, it.productId.toString(), it.totalSales.toDouble(), ttl)
        }

        return result
    }
}
