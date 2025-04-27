package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kr.hhplus.be.server.support.exception.product.ProductMissingIdException
import kr.hhplus.be.server.support.exception.product.ProductOptionMismatchException
import kr.hhplus.be.server.support.exception.product.ProductOptionNotFoundException
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
            val product = Product.create(name = "청바지", basePrice = 10_000)
            val expectedOptions = listOf(
                ProductOption.create(product, optionName = "S", extraPrice = 0),
                ProductOption.create(product, optionName = "M", extraPrice = 1000),
                ProductOption.create(product, optionName = "L", extraPrice = 2000)
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
            val product = Product.create(name = "청바지", basePrice = 10_000)
            val expectedOption = ProductOption.create(product, optionName = "S", extraPrice = 0)
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
            val exception = assertThrows<ProductOptionNotFoundException> {
                productOptionService.getById(optionId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(ProductOptionNotFoundException::class.java)
        }
    }

    @Nested
    inner class ValidateOptionBelongsToProduct {
        private val productId = 1L
        private val optionId = 100L
        private val product = spyk(Product.create(name = "청바지", basePrice = 39000))

        @Test
        @DisplayName("정상적으로 상품 옵션이 상품에 속하는 경우 통과")
        fun whenOptionBelongsToProduct_thenPass() {
            // given
            val option = spyk(ProductOption.create(product = product, optionName = "M", extraPrice = 0))
            every { product.id } returns productId
            every { option.id } returns optionId
            every { productOptionRepository.findById(optionId) } returns option

            // when & then
            productOptionService.validateOptionBelongsToProduct(optionId, productId)
        }

        @Test
        @DisplayName("optionId 또는 productId가 null 인 경우 예외 발생")
        fun whenIdsAreNull_thenThrow() {
            // given
            val optionId = null
            val productId = null

            // when
            val exception = assertThrows<ProductMissingIdException> {
                productOptionService.validateOptionBelongsToProduct(optionId, productId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(ProductMissingIdException::class.java)
                .hasMessage("상품 ID 또는 상품 옵션 ID가 존재하지 않습니다.")
        }

        @Test
        @DisplayName("상품 옵션이 존재하지 않을 경우 예외 발생")
        fun whenOptionNotFound_thenThrow() {
            // given
            every { productOptionRepository.findById(optionId) } returns null

            // when
            val exception = assertThrows<ProductOptionNotFoundException> {
                productOptionService.validateOptionBelongsToProduct(optionId, productId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(ProductOptionNotFoundException::class.java)
        }

        @Test
        @DisplayName("상품 옵션이 다른 상품에 속해 있는 경우 예외 발생")
        fun whenOptionDoesNotBelongToProduct_thenThrow() {
            // given
            val otherProduct = Product.create(name = "후드티", basePrice = 29000)
            val option = ProductOption.create(product = otherProduct, optionName = "S", extraPrice = 0)
            every { productOptionRepository.findById(optionId) } returns option

            // when
            val exception = assertThrows<ProductOptionMismatchException> {
                productOptionService.validateOptionBelongsToProduct(optionId, productId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(ProductOptionMismatchException::class.java)
                .hasMessage("상품 옵션이 해당 상품에 속하지 않습니다.")
        }
    }
}
