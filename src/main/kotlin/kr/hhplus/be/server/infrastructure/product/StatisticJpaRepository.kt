package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Statistic
import org.springframework.data.jpa.repository.JpaRepository

interface StatisticJpaRepository : JpaRepository<Statistic, Long>
