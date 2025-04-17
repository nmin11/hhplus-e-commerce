package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infrastructure.product.PopularProductRecord
import java.time.LocalDateTime

interface StatisticRepository {
    fun save(statistic: Statistic): Statistic
    fun findTop5ProductSales(since: LocalDateTime): List<PopularProductRecord>
}
