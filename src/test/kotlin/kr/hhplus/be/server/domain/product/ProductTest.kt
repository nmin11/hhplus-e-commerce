package kr.hhplus.be.server.domain.product

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class ProductTest {
    @Nested
    inner class Create {
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
            assertThat(product.productOptions).isEmpty()
            assertThat(product.statistics).isEmpty()
        }

        @Test
        @DisplayName("이름이 공백일 경우 예외 발생")
        fun throwException_whenNameIsBlank() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                Product.create(" ", 30000)
            }

            assertThat(exception.message).isEqualTo("상품 이름은 공백일 수 없습니다.")
        }

        @Test
        @DisplayName("기본 가격이 음수일 경우 예외 발생")
        fun throwException_whenBasePriceIsNegative() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                Product.create("청바지", -1)
            }

            assertThat(exception.message).isEqualTo("기본 가격은 0 이상이어야 합니다.")
        }
    }

    @Nested
    inner class RequireSavedId {
        @Test
        @DisplayName("ID가 존재할 경우 해당 ID 반환")
        fun shouldReturnId_whenExists() {
            // given
            val product = Product.create("청바지", 30000).apply { id = 42L }

            // when
            val result = product.requireSavedId()

            // then
            assertThat(result).isEqualTo(42L)
        }

        @Test
        @DisplayName("ID가 null 일 경우 예외 발생")
        fun shouldThrowException_whenIdIsNull() {
            // given
            val product = Product.create("청바지", 30000)

            // when
            val exception = assertThrows(IllegalStateException::class.java) {
                product.requireSavedId()
            }

            // then
            assertThat(exception.message).isEqualTo("Product 객체가 저장되지 않았습니다.")
        }
    }
}
