package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceRepository
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class BalanceFacadeIntegrationTest @Autowired constructor(
    private val balanceFacade: BalanceFacade,
    private val balanceRepository: BalanceRepository,
    private val customerRepository: CustomerRepository
) {
    private lateinit var customer: Customer

    @BeforeEach
    fun setup() {
        customer = Customer.create("test-user")
        customerRepository.save(customer)

        val balance = Balance.create(customer, 100_000)
        balanceRepository.save(balance)
    }

    @Test
    @DisplayName("사용자가 잔액을 충전하면 잔액이 증가하고 이력이 저장됨")
    fun charge_shouldIncreaseBalanceAndRecordHistory() {
        // given
        val command = BalanceCommand.Charge(customer.id, 50_000)

        // when
        val result = balanceFacade.charge(command)

        // then
        assertThat(result.amount).isEqualTo(150_000)
        val histories = balanceFacade.getHistories(customer.id)
        assertThat(histories).hasSize(1)
        assertThat(histories.first().changeAmount).isEqualTo(50_000)
    }

    @Test
    @DisplayName("사용자의 현재 잔액 조회")
    fun getBalance_shouldReturnCurrentBalance() {
        // given
        val command = BalanceCommand.Charge(customer.id, 20_000)
        balanceFacade.charge(command)

        // when
        val result = balanceFacade.getBalance(customer.id)

        // then
        assertThat(result.amount).isEqualTo(120_000)
    }

    @Test
    @DisplayName("사용자의 잔액 변경 내역 조회")
    fun getHistories_shouldReturnAllChangeRecords() {
        // given
        repeat(3) {
            balanceFacade.charge(BalanceCommand.Charge(customer.id, 10_000))
        }

        // when
        val histories = balanceFacade.getHistories(customer.id)

        // then
        assertThat(histories).hasSize(3)
        assertThat(histories).allMatch { it.changeAmount == 10_000 }
    }
}
