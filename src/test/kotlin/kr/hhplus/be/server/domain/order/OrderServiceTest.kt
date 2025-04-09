package kr.hhplus.be.server.domain.order

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OrderServiceTest {
    private val orderRepository = mockk<OrderRepository>()
    private val orderService = OrderService(orderRepository)

    @Test
    @DisplayName("주문을 저장하고 반환")
    fun create_shouldSaveAndReturnOrder() {
        // given
        val customer = Customer("tester").apply { id = 1L }
        val order = Order(
            customer = customer,
            totalPrice = 70000,
        ).apply { id = 1L }

        every { orderRepository.save(order) } returns order

        // when
        val result = orderService.create(order)

        // then
        assertThat(result).isEqualTo(order)
        verify(exactly = 1) { orderRepository.save(order) }
    }
}
