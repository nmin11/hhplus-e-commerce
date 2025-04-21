package kr.hhplus.be.server.infrastructure.balance

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.domain.balance.Balance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface BalanceJpaRepository : JpaRepository<Balance, Long> {
    fun save(balance: Balance): Balance
    fun findByCustomerId(customerId: Long): Balance?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Balance b WHERE b.customer.id = :customerId")
    fun findWithLockByCustomerId(customerId: Long): Balance?
}
