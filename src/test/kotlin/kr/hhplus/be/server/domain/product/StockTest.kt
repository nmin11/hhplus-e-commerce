package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.support.exception.product.StockInvalidQuantityException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class StockTest {
    private val product = Product.create("청바지", 30000)
    private val option = ProductOption.create(product, "M", 1000)

    @Test
    @DisplayName("재고 수량이 0 이상이면 Stock 생성 성공")
    fun create_shouldSucceed_whenQuantityIsValid() {
        // when
        val stock = Stock.create(option, 10)

        // then
        assertThat(stock.productOption).isEqualTo(option)
        assertThat(stock.quantity).isEqualTo(10)
    }

    @Test
    @DisplayName("재고 수량이 0 미만이면 예외 발생")
    fun create_throwException_whenQuantityIsNegative() {
        // when
        val exception = assertThrows(StockInvalidQuantityException::class.java) {
            Stock.create(option, -5)
        }

        // then
        assertThat(exception.message).isEqualTo("재고 수량은 0 이상이어야 합니다.")
    }
}
