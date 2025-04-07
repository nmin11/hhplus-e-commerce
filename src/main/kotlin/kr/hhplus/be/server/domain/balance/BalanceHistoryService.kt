package kr.hhplus.be.server.domain.balance

import org.springframework.stereotype.Service

@Service
class BalanceHistoryService(
    private val balanceHistoryRepository: BalanceHistoryRepository
) {
    fun create(balanceHistory: BalanceHistory): BalanceHistory {
        return balanceHistoryRepository.save(balanceHistory)
    }

    fun getAllByCustomerId(customerId: Long): List<BalanceHistory> {
        return balanceHistoryRepository.findAllByCustomerId(customerId)
    }
}
