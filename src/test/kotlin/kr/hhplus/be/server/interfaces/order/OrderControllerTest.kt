package kr.hhplus.be.server.interfaces.order

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.order.OrderCommand
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.OrderResult
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class OrderControllerTest {
    private val orderFacade = mockk<OrderFacade>()
    private val orderController = OrderController(orderFacade)

    @Test
    @DisplayName("주문 생성 요청 시 주문 응답 반환")
    fun create_shouldReturnCreatedResponse() {
        // given
        val request = OrderRequest.Create(
            customerId = 1L,
            items = listOf(
                OrderRequest.OrderItem(productId = 1L, productOptionId = 2L, quantity = 1),
                OrderRequest.OrderItem(productId = 2L, productOptionId = 3L, quantity = 1)
            )
        )

        val customer = Customer.create("tester").apply { id = 1L }
        val product1 = Product("청바지", 39000).apply { id = 1L }
        val product2 = Product("후드티", 29000).apply { id = 2L }
        val option1 = ProductOption(product1, "M", 1000).apply { id = 2L }
        val option2 = ProductOption(product2, "L", 2000).apply { id = 3L }

        val order = Order.create(customer).apply {
            id = 1L
            totalPrice = 87000
            orderItems.addAll(
                listOf(
                    OrderItem(order = this, productOption = option1, quantity = 1, subtotalPrice = 39000),
                    OrderItem(order = this, productOption = option2, quantity = 1, subtotalPrice = 48000)
                )
            )
        }

        val command = OrderCommand.Create.from(request)
        val result = OrderResult.Create.from(order)
        every { orderFacade.createOrder(command) } returns result

        // when
        val response = orderController.create(request)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo(OrderResponse.Create.from(result))
    }
}
