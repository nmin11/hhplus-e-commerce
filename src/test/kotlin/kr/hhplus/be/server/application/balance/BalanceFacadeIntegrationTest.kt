package kr.hhplus.be.server.application.balance

import io.mockk.spyk
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.config.jpa.JpaConfig
import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Import(TestcontainersConfiguration::class, JpaConfig::class)
@ActiveProfiles("test")
class BalanceFacadeIntegrationTest @Autowired constructor(
    private val balanceFacade: BalanceFacade,
    private val customerRepository: CustomerRepository
) {
    @Test
    @DisplayName("충전 시 잔액이 증가하고 이력이 저장됨")
    fun charge_shouldIncreaseBalanceAndRecordHistory() {
        // given
        val customer = spyk(Customer.create("test-user"))
        Balance.create(customer, 1000)
        val command = BalanceCommand.Charge(customer.id, 1500)

        // when
        val result = balanceFacade.charge(command)

        // then
        assertThat(result.amount).isEqualTo(2500)

        val histories = balanceFacade.getHistories(customer.id)
        assertThat(histories).hasSize(1)
        assertThat(histories.first().changeAmount).isEqualTo(2500)
    }

    @Test
    @DisplayName("사용자의 현재 잔액 조회")
    fun getBalance_shouldReturnCurrentBalance() {
        // given
        val customer = spyk(Customer.create("test-user"))
        Balance.create(customer, 0)
        balanceFacade.charge(BalanceCommand.Charge(customer.id, 2000))

        // when
        val balance = balanceFacade.getBalance(customer.id)

        // then
        assertThat(balance.amount).isEqualTo(2000)
    }

    @Test
    @DisplayName("사용자의 잔액 변경 내역 조회")
    fun getHistories_shouldReturnAllChangeRecords() {
        // given
        val customer = spyk(Customer.create("test-user"))
        Balance.create(customer, 0)
        repeat(3) {
            balanceFacade.charge(BalanceCommand.Charge(customer.id, 1000))
        }

        // when
        val histories = balanceFacade.getHistories(customer.id)

        // then
        assertThat(histories).hasSize(3)
        assertThat(histories).allMatch { it.changeAmount == 1000 }
    }
}
