package kr.hhplus.be.server.domain.customer

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.support.exception.customer.CustomerNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerServiceTest {
    private val customerRepository = mockk<CustomerRepository>()
    private val customerService = CustomerService(customerRepository)

    @Test
    @DisplayName("존재하는 고객에 대한 조회 성공")
    fun validateCustomerExistence_success() {
        // given
        val customerId = 1L
        every { customerRepository.existsById(customerId) } returns true

        // when & then
        customerService.validateCustomerExistence(customerId)
    }

    @Test
    @DisplayName("존재하지 않는 고객 조회 시 예외 발생")
    fun validateCustomerExistence_customerNotExist() {
        // given
        val customerId = 999L
        every { customerRepository.existsById(customerId) } returns false

        // when
        val exception = assertThrows<CustomerNotFoundException> {
            customerService.validateCustomerExistence(customerId)
        }

        // then
        assertThat(exception)
            .isInstanceOf(CustomerNotFoundException::class.java)
            .hasMessage("사용자를 찾을 수 없습니다.")
    }
}
