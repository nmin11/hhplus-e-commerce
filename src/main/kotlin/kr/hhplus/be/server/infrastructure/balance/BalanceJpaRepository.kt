package kr.hhplus.be.server.infrastructure.balance

import kr.hhplus.be.server.domain.balance.Balance
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceJpaRepository : JpaRepository<Balance, Long> {
    fun save(balance: Balance): Balance
    fun findByCustomerId(customerId: Long): Balance?
}
