package kr.hhplus.be.server.domain.customer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerTest {
    @Nested
    inner class Create {
        @Test
        @DisplayName("사용자 이름이 유효하면 Customer 객체 생성")
        fun createCustomer_withValidUsername_shouldSucceed() {
            // when
            val customer = Customer.create("tester")

            // then
            assertThat(customer.username).isEqualTo("tester")
        }

        @Test
        @DisplayName("사용자 이름이 공백이면 예외 발생")
        fun createCustomer_withBlankUsername_shouldThrowException() {
            // given
            val invalidUsernames = listOf("", " ", "\n", "\t")

            invalidUsernames.forEach { name ->
                // when
                val exception = assertThrows<IllegalArgumentException> {
                    Customer.create(name)
                }

                // then
                assertThat(exception.message).isEqualTo("사용자 이름은 비어있을 수 없습니다.")
            }
        }
    }
}
