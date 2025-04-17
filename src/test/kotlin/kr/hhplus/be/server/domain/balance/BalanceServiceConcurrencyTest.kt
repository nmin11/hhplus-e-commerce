package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.CountDownLatch
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

    @BeforeEach
    fun setup() {
        customer = Customer.create("concurrent-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, 10_000)
        balanceRepository.save(balance)
    }

    @Test
    @DisplayName("잔액이 음수가 되도록 하는 잔액 차감 요청 동시성 테스트")
    fun concurrentDeduct_shouldCauseRaceCondition() {
        // given
        val numberOfThreads = 10
        val deductAmount = 3_000
        val latch = CountDownLatch(numberOfThreads)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        repeat(numberOfThreads) {
            executor.submit {
                try {
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

        assertThat(resultBalance).isEqualTo(1_000)
        assertThat(exceptions.isNotEmpty())
    }
}
