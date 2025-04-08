package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StatisticService(
    private val statisticRepository: StatisticRepository
) {
    fun getTop5PopularProductStatistics(since: LocalDate): List<Statistic> {
        return statisticRepository.findTop5BySoldAtAfterOrderBySalesCountDesc(since)
    }
}
