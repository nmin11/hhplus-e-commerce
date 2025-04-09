package kr.hhplus.be.server.infrastructure.dataplatform

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class MockApiDataPlatformSenderTest {

    private val restClient = mockk<RestClient>()
    private val requestSpec = mockk<RestClient.RequestBodyUriSpec>()
    private val responseSpec = mockk<RestClient.ResponseSpec>()

    private val sender = MockApiDataPlatformSender(restClient)

    @Test
    @DisplayName("RestClient 가 올바르게 호출되면 정상 수행")
    fun send_shouldPostOrderData() {
        // given
        val customer = Customer("tester").apply { id = 1L }
        val product = Product("청바지", 39000).apply { id = 1L }
        val option = ProductOption(product, "M", 1000).apply { id = 2L }
        val item = OrderItem(order = mockk(), productOption = option, quantity = 1, subtotalPrice = 40000)
        val order = Order(customer, totalPrice = 40_000).apply {
            id = 123L
            orderItems.add(item)
        }

        every { restClient.post() } returns requestSpec
        every { requestSpec.uri(any<String>()) } returns requestSpec
        every { requestSpec.contentType(MediaType.APPLICATION_JSON) } returns requestSpec
        every { requestSpec.body(any()) } returns requestSpec
        every { requestSpec.retrieve() } returns responseSpec
        every { responseSpec.body(String::class.java) } returns "success"

        // when
        sender.send(order)

        // then
        verify(exactly = 1) { restClient.post() }
        verify { requestSpec.uri("https://67f65cb942d6c71cca61b523.mockapi.io/order") }
    }
}
