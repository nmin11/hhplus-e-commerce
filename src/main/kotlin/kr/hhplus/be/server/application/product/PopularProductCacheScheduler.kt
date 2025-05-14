package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.StatisticService
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDate

@Deprecated("Redis Sorted Set 기반으로 대체 예정")
class PopularProductCacheScheduler(
    private val statisticService: StatisticService,
) {
    // TODO: Redis 랭킹 시스템 구현 후 제거
    // 매일 11시 30분, 23시 30분에 12시간 간격으로 인기 상품 조회 Cache Put
    // @Scheduled(cron = "0 30 11,23 * * *")
    fun warmPopularProductsCache() {
        statisticService.cachePopularProducts(LocalDate.now().minusDays(3L))
    }
}
