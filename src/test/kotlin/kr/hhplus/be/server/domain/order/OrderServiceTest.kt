package kr.hhplus.be.server.domain.order

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderServiceTest {
    private val orderRepository = mockk<OrderRepository>()
    private val orderService = OrderService(orderRepository)

    @Nested
    inner class Create {
        @Test
        @DisplayName("주문을 저장하고 반환")
        fun shouldSaveAndReturnOrder() {
            // given
            val customer = Customer("tester").apply { id = 1L }
            val order = Order(
                customer = customer,
                totalPrice = 70000
            ).apply { id = 1L }

            every { orderRepository.save(order) } returns order

            // when
            val result = orderService.create(order)

            // then
            assertThat(result).isEqualTo(order)
            verify(exactly = 1) { orderRepository.save(order) }
        }
    }

    @Nested
    inner class GetById {
        private val orderId = 1L

        @Test
        @DisplayName("ID로 주문을 조회하여 반환")
        fun returnOrder_whenExists() {
            // given
            val customer = Customer("tester").apply { id = 1L }
            val expectedOrder = Order(customer = customer, totalPrice = 50000).apply { id = orderId }

            every { orderRepository.findById(orderId) } returns expectedOrder

            // when
            val result = orderService.getById(orderId)

            // then
            assertThat(result).isEqualTo(expectedOrder)
        }

        @Test
        @DisplayName("주문이 존재하지 않을 경우 예외 발생")
        fun throwException_whenOrderNotFound() {
            // given
            every { orderRepository.findById(orderId) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                orderService.getById(orderId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("주문 정보를 찾을 수 없습니다.")
        }
    }
}
