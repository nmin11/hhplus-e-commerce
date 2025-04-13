package kr.hhplus.be.server.domain.product

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class ProductOptionTest {
    private val product = Product.create("청바지", 30000).apply { id = 1L }

    @Nested
    inner class Create {
        @Test
        @DisplayName("정상적인 옵션명과 추가 가격으로 ProductOption 생성 성공")
        fun create_shouldReturnProductOption_whenValidInput() {
            // when
            val option = ProductOption.create(product, "M", 1000)

            // then
            assertThat(option.product).isEqualTo(product)
            assertThat(option.optionName).isEqualTo("M")
            assertThat(option.extraPrice).isEqualTo(1000)
            assertThat(option.stock).isNull()
            assertThat(option.orderItems).isEmpty()
        }

        @Test
        @DisplayName("옵션명이 공백일 경우 예외 발생")
        fun throwException_whenOptionNameIsBlank() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                ProductOption.create(product, " ", 1000)
            }

            assertThat(exception.message).isEqualTo("옵션 이름은 공백일 수 없습니다.")
        }

        @Test
        @DisplayName("추가 가격이 음수일 경우 예외 발생")
        fun throwException_whenExtraPriceIsNegative() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                ProductOption.create(product, "M", -500)
            }

            assertThat(exception.message).isEqualTo("추가 가격은 0 이상이어야 합니다.")
        }
    }

    @Nested
    inner class RequireSavedId {
        @Test
        @DisplayName("ID가 존재하면 해당 ID 반환")
        fun shouldReturnId_whenExists() {
            // given
            val option = ProductOption.create(product, "M", 1000).apply { id = 42L }

            // when
            val result = option.requireSavedId()

            // then
            assertThat(result).isEqualTo(42L)
        }

        @Test
        @DisplayName("ID가 null 일 경우 예외 발생")
        fun shouldThrowException_whenIdIsNull() {
            // given
            val option = ProductOption.create(product, "M", 1000)

            // when
            val exception = assertThrows(IllegalStateException::class.java) {
                option.requireSavedId()
            }

            // then
            assertThat(exception.message).isEqualTo("ProductOption 객체가 저장되지 않았습니다.")
        }
    }
}
