package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Statistic
import kr.hhplus.be.server.domain.product.StatisticRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class StatisticRepositoryImpl(
    private val statisticJpaRepository: StatisticJpaRepository,
    private val statisticJooqRepository: StatisticJooqRepository
) : StatisticRepository {
    override fun save(statistic: Statistic): Statistic {
        return statisticJpaRepository.save(statistic)
    }

    override fun findTop5ProductSales(since: LocalDateTime): List<PopularProductRecord> {
        return statisticJooqRepository.findTop5ProductSales(since)
    }
}
