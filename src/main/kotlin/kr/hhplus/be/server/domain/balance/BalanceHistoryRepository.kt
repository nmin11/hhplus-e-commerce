package kr.hhplus.be.server.domain.balance

interface BalanceHistoryRepository {
    fun save(balanceHistory: BalanceHistory): BalanceHistory
    fun findAllByCustomerId(customerId: Long): List<BalanceHistory>
}
