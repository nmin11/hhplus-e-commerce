package kr.hhplus.be.server.domain.product

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class ProductTest {
    @Test
    @DisplayName("정상적인 이름과 가격으로 Product 생성 성공")
    fun create_shouldReturnProduct_whenValidInput() {
        // given
        val name = "청바지"
        val basePrice = 30000

        // when
        val product = Product.create(name, basePrice)

        // then
        assertThat(product.name).isEqualTo("청바지")
        assertThat(product.basePrice).isEqualTo(30000)
    }

    @Test
    @DisplayName("이름이 공백일 경우 예외 발생")
    fun create_throwException_whenNameIsBlank() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Product.create(" ", 30000)
        }

        assertThat(exception.message).isEqualTo("상품 이름은 공백일 수 없습니다.")
    }

    @Test
    @DisplayName("기본 가격이 음수일 경우 예외 발생")
    fun create_throwException_whenBasePriceIsNegative() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Product.create("청바지", -1)
        }

        assertThat(exception.message).isEqualTo("기본 가격은 0 이상이어야 합니다.")
    }
}
