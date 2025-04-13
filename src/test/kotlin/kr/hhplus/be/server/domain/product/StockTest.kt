package kr.hhplus.be.server.domain.product

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StockTest {
    private val product = Product.create("청바지", 30000).apply { id = 1L }
    private val option = ProductOption.create(product, "M", 1000).apply { id = 1L }

    @Nested
    inner class Create {
        @Test
        @DisplayName("재고 수량이 0 이상이면 Stock 생성 성공")
        fun shouldSucceed_whenQuantityIsValid() {
            // when
            val stock = Stock.create(option, 10)

            // then
            assertThat(stock.productOption).isEqualTo(option)
            assertThat(stock.quantity).isEqualTo(10)
            assertThat(stock.updatedAt).isNotNull()
        }

        @Test
        @DisplayName("재고 수량이 0 미만이면 예외 발생")
        fun throwException_whenQuantityIsNegative() {
            // when
            val exception = assertThrows(IllegalArgumentException::class.java) {
                Stock.create(option, -5)
            }

            // then
            assertThat(exception.message).isEqualTo("재고 수량은 0 이상이어야 합니다.")
        }
    }

    @Nested
    inner class RequireSavedId {
        @Test
        @DisplayName("ID가 존재하면 해당 ID 반환")
        fun shouldReturnId_whenExists() {
            // given
            val stock = Stock.create(option, 20).apply { id = 99L }

            // when
            val result = stock.requireSavedId()

            // then
            assertThat(result).isEqualTo(99L)
        }

        @Test
        @DisplayName("ID가 null 이면 예외 발생")
        fun shouldThrowException_whenIdIsNull() {
            // given
            val stock = Stock.create(option, 20)

            // when
            val exception = assertThrows(IllegalStateException::class.java) {
                stock.requireSavedId()
            }

            // then
            assertThat(exception.message).isEqualTo("Stock 객체가 저장되지 않았습니다.")
        }
    }
}
