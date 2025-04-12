package kr.hhplus.be.server.domain.payment

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PaymentServiceTest {
    private val paymentRepository = mockk<PaymentRepository>()
    private val paymentService = PaymentService(paymentRepository)

    @Test
    @DisplayName("결제를 저장하고 반환")
    fun create_shouldSaveAndReturnPayment() {
        // given
        val customer = Customer.create(username = "tester").apply { id = 1L }
        val order = Order.create(customer).apply {
            id = 1L
            totalPrice = 50_000
        }
        val payment = Payment(
            order = order,
            customer = customer,
            coupon = null,
            originalPrice = 50_000,
            discountAmount = 0,
            discountedPrice = 50_000,
        ).apply { id = 1L }

        every { paymentRepository.save(payment) } returns payment

        // when
        val result = paymentService.create(payment)

        // then
        assertThat(result).isEqualTo(payment)
        verify(exactly = 1) { paymentRepository.save(payment) }
    }
}
