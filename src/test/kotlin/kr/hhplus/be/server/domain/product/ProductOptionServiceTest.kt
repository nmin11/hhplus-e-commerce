package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductOptionServiceTest {
    private val productOptionRepository = mockk<ProductOptionRepository>()
    private val productOptionService = ProductOptionService(productOptionRepository)

    @Nested
    inner class GetByProductId {
        @Test
        @DisplayName("상품 ID로 해당 옵션들을 반환")
        fun returnOptionsByProductId() {
            // given
            val productId = 1L
            val product = Product(name = "청바지", basePrice = 10_000).apply { id = productId }
            val expectedOptions = listOf(
                ProductOption(product, optionName = "S", extraPrice = 0).apply { id = 1L },
                ProductOption(product, optionName = "M", extraPrice = 1000).apply { id = 2L },
                ProductOption(product, optionName = "L", extraPrice = 2000).apply { id = 3L }
            )
            every { productOptionRepository.findAllByProductId(productId) } returns expectedOptions

            // when
            val result = productOptionService.getByProductId(productId)

            // then
            assertThat(result).isEqualTo(expectedOptions)
        }
    }

    @Nested
    inner class GetById {
        @Test
        @DisplayName("옵션 ID로 상품 옵션을 조회")
        fun returnProductOptionById() {
            // given
            val optionId = 1L
            val product = Product(name = "청바지", basePrice = 10_000).apply { id = 1L }
            val expectedOption = ProductOption(product, optionName = "S", extraPrice = 0).apply { id = optionId }
            every { productOptionRepository.findById(optionId) } returns expectedOption

            // when
            val result = productOptionService.getById(optionId)

            // then
            assertThat(result).isEqualTo(expectedOption)
        }

        @Test
        @DisplayName("존재하지 않는 옵션 ID일 경우 예외 발생")
        fun throwExceptionWhenOptionNotFound() {
            // given
            val optionId = 999L
            every { productOptionRepository.findById(optionId) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                productOptionService.getById(optionId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("상품 옵션 정보가 존재하지 않습니다.")
        }
    }
}
