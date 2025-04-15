package kr.hhplus.be.server.infrastructure.balance

import kr.hhplus.be.server.domain.balance.BalanceHistory
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceHistoryJpaRepository : JpaRepository<BalanceHistory, Long> {
    fun save(balanceHistory: BalanceHistory): BalanceHistory
    fun findAllByCustomerId(customerId: Long): List<BalanceHistory>
}
