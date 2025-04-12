package kr.hhplus.be.server.domain.product

import java.time.LocalDate

interface StatisticRepository {
    fun save(statistic: Statistic): Statistic
    fun findTop5BySoldAtAfterOrderBySalesCountDesc(since: LocalDate): List<Statistic>
}
