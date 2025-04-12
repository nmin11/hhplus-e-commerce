package kr.hhplus.be.server.infrastructure.dataplatform

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.dataplatform.DataPlatformCommand
import kr.hhplus.be.server.application.payment.PaymentCommandFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class MockApiDataPlatformSenderTest {
    private val paymentCommandFactory = mockk<PaymentCommandFactory>()
    private val restClient = mockk<RestClient>()
    private val requestSpec = mockk<RestClient.RequestBodyUriSpec>()
    private val responseSpec = mockk<RestClient.ResponseSpec>()
    private val sender = MockApiDataPlatformSender(restClient)

    @Test
    @DisplayName("RestClient 가 올바르게 호출되면 정상 수행")
    fun send_shouldPostOrderData() {
        // given
        val command = DataPlatformCommand.Order(
            orderId = 123L,
            customerId = 1L,
            totalPrice = 40_000,
            createdAt = "2025-04-10T10:00:00",
            items = listOf(
                DataPlatformCommand.OrderItem(
                    productName = "청바지",
                    optionName = "M",
                    quantity = 1,
                    subtotalPrice = 40_000
                )
            )
        )

        every { restClient.post() } returns requestSpec
        every { requestSpec.uri("https://67f65cb942d6c71cca61b523.mockapi.io/order") } returns requestSpec
        every { requestSpec.contentType(MediaType.APPLICATION_JSON) } returns requestSpec
        every { requestSpec.body(command) } returns requestSpec
        every { requestSpec.retrieve() } returns responseSpec
        every { responseSpec.body(String::class.java) } returns "success"

        // when
        sender.send(command)

        // then
        verify(exactly = 1) { restClient.post() }
        verify(exactly = 1) { requestSpec.uri("https://67f65cb942d6c71cca61b523.mockapi.io/order") }
        verify(exactly = 1) { requestSpec.body(command) }
        verify(exactly = 1) { requestSpec.retrieve() }
    }
}
