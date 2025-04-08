package kr.hhplus.be.server.domain.product

import java.time.LocalDate

interface StatisticRepository {
    fun findTop5BySoldAtAfterOrderBySalesCountDesc(since: LocalDate): List<Statistic>
}
