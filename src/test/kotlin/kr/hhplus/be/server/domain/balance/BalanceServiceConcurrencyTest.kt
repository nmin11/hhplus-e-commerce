package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.support.exception.balance.BalanceChargeFailedException
import kr.hhplus.be.server.support.exception.balance.BalanceDeductFailedException
import kr.hhplus.be.server.support.exception.balance.BalanceInsufficientException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
class BalanceServiceConcurrencyTest @Autowired constructor(
    private val balanceService: BalanceService,
    private val customerRepository: CustomerRepository,
    private val balanceRepository: BalanceRepository
) {
    private lateinit var customer: Customer
    private lateinit var balance: Balance
    val initAmount = 10_000

    @BeforeEach
    fun setup() {
        customer = Customer.create("concurrent-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, initAmount)
        balanceRepository.save(balance)
    }

    @Test
    @DisplayName("잔액이 음수가 되도록 하는 잔액 차감 요청 동시성 테스트")
    fun concurrentDeduct_shouldCauseRaceCondition() {
        // given
        val numberOfThreads = 3
        val deductAmount = 5_000
        val barrier = CyclicBarrier(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        repeat(numberOfThreads) {
            executor.submit {
                try {
                    barrier.await() // 동시 실행 보장
                    balanceService.deduct(customer.id, deductAmount)
                } catch (e: Exception) {
                    exceptions.add(e)
                    println("❗예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // then
        val resultBalance = balanceService.getByCustomerId(customer.id).getAmount()
        println("💰 최종 잔액: $resultBalance.")

        assertThat(resultBalance).isEqualTo(5_000)
        assertThat(exceptions.count { it is BalanceDeductFailedException })
            .isEqualTo(numberOfThreads - 1)
    }

    @Test
    @DisplayName("2번의 동시 충전 요청 시 충돌이 발생해도 재시도 처리로 충전 완료")
    fun concurrentCharge_shouldSucceedWithRetry() {
        // given
        val numberOfThreads = 2
        val chargeAmount = 1_000
        val latch = CountDownLatch(numberOfThreads)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        repeat(numberOfThreads) {
            executor.submit {
                try {
                    balanceService.charge(customer.id, chargeAmount)
                } catch (e: Exception) {
                    exceptions.add(e)
                    println("❗예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // then
        val resultBalance = balanceService.getByCustomerId(customer.id).getAmount()
        println("💰 최종 잔액: $resultBalance.")
        assertThat(resultBalance).isEqualTo(initAmount + (chargeAmount * numberOfThreads))

        val failureCount = exceptions.count { it is BalanceChargeFailedException }
        assertThat(failureCount).isEqualTo(0)
    }

    @Test
    @DisplayName("다수의 동시 충전 요청 시 충돌이 발생해서 일부 요청만 적용")
    fun concurrentCharge_shouldHandleRetryAndFailure() {
        // given
        val numberOfThreads = 5
        val chargeAmount = 1_000
        val latch = CountDownLatch(numberOfThreads)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        repeat(numberOfThreads) {
            executor.submit {
                try {
                    balanceService.charge(customer.id, chargeAmount)
                } catch (e: Exception) {
                    exceptions.add(e)
                    println("❗예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // then
        val resultBalance = balanceService.getByCustomerId(customer.id).getAmount()
        println("💰 최종 잔액: $resultBalance.")

        val chargedAmount = resultBalance - initAmount
        val successCount = chargedAmount / 1_000
        val failureCount = exceptions.count { it is BalanceChargeFailedException }
        assertThat(successCount + failureCount).isEqualTo(numberOfThreads)
    }
}
