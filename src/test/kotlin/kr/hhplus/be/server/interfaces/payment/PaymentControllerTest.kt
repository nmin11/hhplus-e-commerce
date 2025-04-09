package kr.hhplus.be.server.interfaces.payment

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.payment.PaymentFacade
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.payment.Payment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PaymentControllerTest {
    private val paymentFacade = mockk<PaymentFacade>()
    private val paymentController = PaymentController(paymentFacade)

    @Test
    @DisplayName("결제 요청 시 결제 응답 반환")
    fun pay_shouldReturnCreatedResponse() {
        // given
        val request = PaymentRequest(orderId = 1L, couponId = null)

        val customer = Customer("tester").apply { id = 1L }
        val order = Order(customer = customer, totalPrice = 87000).apply { id = 10L }

        val payment = Payment(
            order = order,
            customer = customer,
            coupon = null,
            originalPrice = 87000,
            discountAmount = 0,
            discountedPrice = 87000,
        ).apply {
            id = 100L
        }

        every { paymentFacade.pay(request.orderId, request.couponId) } returns payment

        // when
        val response = paymentController.pay(request)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo(PaymentResponse.from(payment))
    }
}
