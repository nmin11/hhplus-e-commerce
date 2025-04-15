package kr.hhplus.be.server.infrastructure.balance

import kr.hhplus.be.server.domain.balance.BalanceHistory
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository
import org.springframework.stereotype.Repository

@Repository
class BalanceHistoryRepositoryImpl(
    private val balanceHistoryJpaRepository: BalanceHistoryJpaRepository
) : BalanceHistoryRepository {
    override fun save(balanceHistory: BalanceHistory): BalanceHistory {
        return balanceHistoryJpaRepository.save(balanceHistory)
    }

    override fun findAllByCustomerId(customerId: Long): List<BalanceHistory> {
        return balanceHistoryJpaRepository.findAllByCustomerId(customerId)
    }
}
