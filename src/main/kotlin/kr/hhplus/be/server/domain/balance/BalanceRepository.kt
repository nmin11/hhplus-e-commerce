package kr.hhplus.be.server.domain.balance

interface BalanceRepository {
    fun save(balance: Balance): Balance
    fun findByCustomerId(customerId: Long): Balance?
    fun findWithLockByCustomerId(customerId: Long): Balance?
}
