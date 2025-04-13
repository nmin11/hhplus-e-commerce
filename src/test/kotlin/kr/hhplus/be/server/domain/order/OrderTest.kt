package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderTest {
    private val customer = Customer.create("tester").apply { id = 1L }
    private val product = Product.create("청바지", basePrice = 39_000).apply { id = 10L }
    private val option = ProductOption.create(product, "M", extraPrice = 1_000).apply { id = 100L }

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
    inner class RequireSavedId {
        @Test
        @DisplayName("ID가 존재하면 해당 ID 반환")
        fun shouldReturnId_whenExists() {
            // given
            val order = Order.create(customer).apply { id = 99L }

            // when
            val result = order.requireSavedId()

            // then
            assertThat(result).isEqualTo(99L)
        }

        @Test
        @DisplayName("ID가 null 이면 예외 발생")
        fun shouldThrowException_whenIdIsNull() {
            // given
            val order = Order.create(customer)

            // when
            val exception = assertThrows<IllegalStateException> {
                order.requireSavedId()
            }

            // then
            assertThat(exception.message).isEqualTo("Order 객체가 저장되지 않았습니다.")
        }
    }
}
