package kr.hhplus.be.server.domain.payment

import java.time.LocalDate
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PaymentTest {
    private val customer = Customer.create("tester")
    private val order = Order.create(customer)
    private val coupon = Coupon.createFixedDiscount(
        name = "5천원 할인",
        amount = 5000,
        quantity = 100,
        startedAt = LocalDate.now().minusDays(1),
        expiredAt = LocalDate.now().plusDays(1)
    )

    @Nested
    inner class Create {
        @Test
        @DisplayName("할인 없는 결제 정보를 생성할 수 있다")
        fun createPaymentWithoutCoupon() {
            // given
            val originalPrice = 30000
            val discountAmount = 0

            // when
            val payment = Payment.create(order, customer, originalPrice, discountAmount)

            // then
            assertThat(payment.originalPrice).isEqualTo(30000)
            assertThat(payment.discountAmount).isEqualTo(0)
            assertThat(payment.discountedPrice).isEqualTo(30000)
            assertThat(payment.coupon).isNull()
        }

        @Test
        @DisplayName("할인이 적용된 결제 정보를 생성할 수 있다")
        fun createPaymentWithCoupon() {
            // given
            val originalPrice = 40000
            val discountAmount = 5000

            // when
            val payment = Payment.create(order, customer, originalPrice, discountAmount, coupon)

            // then
            assertThat(payment.originalPrice).isEqualTo(40000)
            assertThat(payment.discountAmount).isEqualTo(5000)
            assertThat(payment.discountedPrice).isEqualTo(35000)
            assertThat(payment.coupon).isEqualTo(coupon)
        }

        @Test
        @DisplayName("할인 금액이 음수일 경우 예외 발생")
        fun throwException_whenDiscountIsNegative() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                Payment.create(order, customer, 30000, -1000)
            }

            assertThat(exception.message).isEqualTo("할인 금액은 0 이상이어야 합니다.")
        }

        @Test
        @DisplayName("할인 금액이 원가보다 클 경우 예외 발생")
        fun throwException_whenDiscountExceedsOriginal() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                Payment.create(order, customer, 30000, 40000)
            }

            assertThat(exception.message).isEqualTo("할인 금액은 총 주문 금액보다 낮아야 합니다.")
        }

        @Test
        @DisplayName("원가가 음수일 경우 예외 발생")
        fun throwException_whenOriginalPriceIsNegative() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                Payment.create(order, customer, -1, 0)
            }

            assertThat(exception.message).isEqualTo("기존 금액은 0 이상이어야 합니다.")
        }
    }
}
