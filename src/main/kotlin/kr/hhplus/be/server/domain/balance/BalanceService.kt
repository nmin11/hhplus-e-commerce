package kr.hhplus.be.server.domain.balance

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BalanceService(
    private val balanceRepository: BalanceRepository
) {
    fun getByCustomerId(customerId: Long): Balance {
        return balanceRepository.findByCustomerId(customerId)
            ?: throw IllegalStateException("잔액 정보가 존재하지 않습니다.")
    }

    fun charge(customerId: Long, amount: Int): Balance {
        require(amount > 0) { "충전 금액은 0보다 커야 합니다." }

        val balance = balanceRepository.findByCustomerId(customerId)
            ?: throw IllegalStateException("잔액 정보가 존재하지 않습니다.")

        balance.amount += amount
        balance.updatedAt = LocalDateTime.now()

        balanceRepository.save(balance)
        return balance
    }
}
