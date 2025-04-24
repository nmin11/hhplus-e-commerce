package kr.hhplus.be.server.domain.balance

import org.slf4j.LoggerFactory
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BalanceService(
    private val balanceRepository: BalanceRepository
) {
    private val log = LoggerFactory.getLogger(BalanceService::class.java)

    fun getByCustomerId(customerId: Long): Balance {
        return balanceRepository.findByCustomerId(customerId)
            ?: throw IllegalStateException("잔액 정보가 존재하지 않습니다.")
    }

    @Retryable(
        value = [ObjectOptimisticLockingFailureException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 100)
    )
    fun charge(customerId: Long, amount: Int): Balance {
        val balance = getByCustomerId(customerId)
        balance.charge(amount)
        return balanceRepository.saveAndFlush(balance)
    }

    @Recover
    fun recoverCharge(e: ObjectOptimisticLockingFailureException, customerId: Long, amount: Int): Balance {
        log.warn("충전 재시도 실패: customerId=$customerId, amount=$amount, message=${e.javaClass.simpleName}")
        throw IllegalStateException("지금은 충전을 진행할 수 없습니다. 잠시 후 다시 시도해주세요.")
    }

    @Transactional
    fun deduct(customerId: Long, amount: Int): Balance {
        try {
            val balance = getByCustomerId(customerId)
            balance.deduct(amount)
            return balanceRepository.saveAndFlush(balance)
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw IllegalStateException("지금은 결제를 진행할 수 없습니다. 잠시 후 다시 시도해주세요.")
        }
    }
}
