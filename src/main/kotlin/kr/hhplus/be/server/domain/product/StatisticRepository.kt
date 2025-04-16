package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

interface StatisticRepository {
    fun save(statistic: Statistic): Statistic
    fun findTop5BySoldAtAfterOrderBySalesCountDesc(since: LocalDateTime): List<Statistic>
}
