package kr.hhplus.be.server.domain.balance

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    @Transactional
    fun deduct(customerId: Long, amount: Int): Balance {
        val balance = getWithLockByCustomerId(customerId)
        balance.deduct(amount)
        return balanceRepository.save(balance)
    }

    private fun getWithLockByCustomerId(customerId: Long): Balance {
        return balanceRepository.findWithLockByCustomerId(customerId)
            ?: throw IllegalStateException("잔액 정보가 존재하지 않습니다.")
    }
}
