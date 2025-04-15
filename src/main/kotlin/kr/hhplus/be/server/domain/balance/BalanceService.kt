package kr.hhplus.be.server.domain.balance

import org.springframework.stereotype.Service

@Service
class BalanceService(
    private val balanceRepository: BalanceRepository
) {
    fun getByCustomerId(customerId: Long): Balance {
        return balanceRepository.findByCustomerId(customerId)
            ?: throw IllegalStateException("잔액 정보가 존재하지 않습니다.")
    }

    fun charge(customerId: Long, amount: Int): Balance {
        val balance = getByCustomerId(customerId)
        balance.charge(amount)
        return balanceRepository.save(balance)
    }

    fun deduct(customerId: Long, amount: Int): Balance {
        val balance = getByCustomerId(customerId)
        balance.deduct(amount)
        return balanceRepository.save(balance)
    }
}
