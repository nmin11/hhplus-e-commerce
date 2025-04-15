package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Statistic
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface StatisticJpaRepository : JpaRepository<Statistic, Long> {
    fun findTop5BySoldAtAfterOrderBySalesCountDesc(since: LocalDate): List<Statistic>
}
