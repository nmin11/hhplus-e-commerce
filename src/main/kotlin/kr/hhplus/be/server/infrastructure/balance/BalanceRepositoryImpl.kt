package kr.hhplus.be.server.infrastructure.balance

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceRepository
import org.springframework.stereotype.Repository

@Repository
class BalanceRepositoryImpl(
    private val balanceJpaRepository: BalanceJpaRepository
) : BalanceRepository {
    override fun save(balance: Balance): Balance {
        return balanceJpaRepository.save(balance)
    }

    override fun findByCustomerId(customerId: Long): Balance? {
        return balanceJpaRepository.findByCustomerId(customerId)
    }
}
