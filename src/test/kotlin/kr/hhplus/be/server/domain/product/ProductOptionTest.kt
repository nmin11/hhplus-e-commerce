package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.support.exception.product.ProductOptionInvalidExtraPriceException
import kr.hhplus.be.server.support.exception.product.ProductOptionNameBlankException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class ProductOptionTest {
    private val product = Product.create("청바지", 30000)

    @Test
    @DisplayName("정상적인 옵션명과 추가 가격으로 ProductOption 생성 성공")
    fun create_shouldReturnProductOption_whenValidInput() {
        // when
        val option = ProductOption.create(product, "M", 1000)

        // then
        assertThat(option.product).isEqualTo(product)
        assertThat(option.optionName).isEqualTo("M")
        assertThat(option.extraPrice).isEqualTo(1000)
    }

    @Test
    @DisplayName("옵션명이 공백일 경우 예외 발생")
    fun create_throwException_whenOptionNameIsBlank() {
        val exception = assertThrows(ProductOptionNameBlankException::class.java) {
            ProductOption.create(product, " ", 1000)
        }

        assertThat(exception.message).isEqualTo("옵션 이름은 공백일 수 없습니다.")
    }

    @Test
    @DisplayName("추가 가격이 음수일 경우 예외 발생")
    fun create_throwException_whenExtraPriceIsNegative() {
        val exception = assertThrows(ProductOptionInvalidExtraPriceException::class.java) {
            ProductOption.create(product, "M", -500)
        }

        assertThat(exception.message).isEqualTo("추가 가격은 0 이상이어야 합니다.")
    }
}
