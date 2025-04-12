package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductServiceTest {
    private val productRepository = mockk<ProductRepository>()
    private val productService = ProductService(productRepository)

    @Nested
    inner class GetAll {
        @Test
        @DisplayName("모든 상품 목록 반환")
        fun returnAllProducts() {
            // given
            val products = listOf(
                Product.create(name = "청바지", basePrice = 39000),
                Product.create(name = "후드티", basePrice = 29000)
            )
            every { productRepository.findAll() } returns products

            // when
            val result = productService.getAll()

            // then
            assertThat(result).isEqualTo(products)
        }
    }

    @Nested
    inner class GetAllByIds {
        @Test
        @DisplayName("지정된 ID에 해당하는 상품들 반환")
        fun returnMatchingProducts() {
            // given
            val ids = listOf(1L, 2L)
            val products = listOf(
                Product.create(name = "청바지", basePrice = 39000).apply { id = 1L },
                Product.create(name = "후드티", basePrice = 29000).apply { id = 2L }
            )
            every { productRepository.findAllByIds(ids) } returns products

            // when
            val result = productService.getAllByIds(ids)

            // then
            assertThat(result).isEqualTo(products)
        }
    }

    @Nested
    inner class GetById {
        private val productId = 1L

        @Test
        @DisplayName("조회한 상품 반환")
        fun returnProduct_whenExists() {
            // given
            val expectedProduct = Product.create(name = "청바지", basePrice = 39000)
            every { productRepository.findById(productId) } returns expectedProduct

            // when
            val result = productService.getById(productId)

            // then
            assertThat(result).isEqualTo(expectedProduct)
        }

        @Test
        @DisplayName("존재하지 않는 상품일 경우 예외 발생")
        fun throwException_whenNotFound() {
            // given
            every { productRepository.findById(productId) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                productService.getById(productId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("상품 정보가 존재하지 않습니다.")
        }
    }
}
