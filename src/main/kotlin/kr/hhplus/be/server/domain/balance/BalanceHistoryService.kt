package kr.hhplus.be.server.domain.balance

import org.springframework.stereotype.Service

@Service
class BalanceHistoryService(
    private val balanceHistoryRepository: BalanceHistoryRepository
) {
    fun getAllByCustomerId(customerId: Long): List<BalanceHistory> {
        return balanceHistoryRepository.findAllByCustomerId(customerId)
    }
}
