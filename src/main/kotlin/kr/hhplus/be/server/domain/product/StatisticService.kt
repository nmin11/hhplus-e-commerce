package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StatisticService(
    private val statisticRepository: StatisticRepository
) {
    fun record(statistic: Statistic): Statistic {
        return statisticRepository.save(statistic)
    }

    fun getTop5PopularProductStatistics(since: LocalDate): List<Statistic> {
        val startOfDay = since.atStartOfDay()
        return statisticRepository.findTop5BySoldAtAfterOrderBySalesCountDesc(startOfDay)
    }
}
