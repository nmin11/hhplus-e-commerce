package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption
import kr.hhplus.be.server.support.exception.order.OrderItemEmptyException
import kr.hhplus.be.server.support.exception.order.OrderNotPayableException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrderTest {
    private val customer = Customer.create("tester")
    private val product = Product.create("청바지", basePrice = 39_000)
    private val option = ProductOption.create(product, "M", extraPrice = 1_000)

    @Nested
    inner class Create {
        @Test
        @DisplayName("고객 정보를 기반으로 주문 객체를 생성")
        fun createWithCustomer_shouldSucceed() {
            // when
            val order = Order.create(customer)

            // then
            assertThat(order.customer).isEqualTo(customer)
            assertThat(order.status).isEqualTo(OrderStatus.CREATED)
            assertThat(order.totalPrice).isEqualTo(0)
            assertThat(order.orderItems).isEmpty()
        }
    }

    @Nested
    inner class CreateOrderWithItems {
        @Test
        @DisplayName("상품 옵션과 수량을 포함한 주문 객체를 생성")
        fun shouldAddItemsAndCalculateTotalPrice() {
            // given
            val quantity = 2
            val items = listOf(OrderItemInfo(option, quantity))

            // when
            val order = Order.createWithItems(customer, items)

            // then
            assertThat(order.customer).isEqualTo(customer)
            assertThat(order.orderItems).hasSize(1)
            assertThat(order.totalPrice).isEqualTo((product.basePrice + option.extraPrice) * quantity)
        }

        @Test
        @DisplayName("주문 항목이 비어 있으면 예외 발생")
        fun shouldThrowExceptionWhenItemsAreEmpty() {
            // given
            val emptyItems = emptyList<OrderItemInfo>()

            // when & then
            assertThatThrownBy { Order.createWithItems(customer, emptyItems) }
                .isInstanceOf(OrderItemEmptyException::class.java)
                .hasMessage("주문 항목이 비어있습니다.")
        }
    }

    @Nested
    inner class AddOrderItem {
        @Test
        @DisplayName("주문 항목을 추가하면 orderItems 에 추가되고 totalPrice 가 갱신됨")
        fun addOrderItem_shouldAppendItemAndUpdateTotalPrice() {
            // given
            val order = Order.create(customer)

            // when
            order.addOrderItem(option, quantity = 1)

            // then
            assertThat(order.orderItems).hasSize(1)
            assertThat(order.totalPrice).isEqualTo(product.basePrice + option.extraPrice)
        }
    }

    @Nested
    inner class MarkAsPaid {
        @Test
        @DisplayName("주문 상태가 CREATED 일 때 PAID 로 변경됨")
        fun shouldMarkOrderAsPaidWhenStatusIsCreated() {
            // given
            val order = Order.create(customer)

            // when
            order.markAsPaid()

            // then
            assertThat(order.status).isEqualTo(OrderStatus.PAID)
        }

        @Test
        @DisplayName("주문 상태가 PAID 일 경우 예외 발생")
        fun shouldThrowExceptionWhenOrderAlreadyPaid() {
            // given
            val order = Order.create(customer)
            order.markAsPaid()

            // when & then
            assertThatThrownBy { order.markAsPaid() }
                .isInstanceOf(OrderNotPayableException::class.java)
        }
    }
}
