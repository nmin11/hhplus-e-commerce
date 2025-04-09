package kr.hhplus.be.server.interfaces.product

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.ProductResult
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption
import kr.hhplus.be.server.domain.product.ProductService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ProductControllerTest {
    private val productService = mockk<ProductService>()
    private val productFacade = mockk<ProductFacade>()
    private val productController = ProductController(productFacade, productService)

    @Test
    @DisplayName("전체 상품 목록 조회")
    fun getAllProduct_shouldReturnListOfSummaries() {
        // given
        val products = listOf(
            Product(name = "청바지", basePrice = 39000).apply { id = 1L },
            Product(name = "후드티", basePrice = 29000).apply { id = 2L }
        )
        every { productService.getAll() } returns products

        // when
        val response = productController.getAllProduct()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.size).isEqualTo(2)
        assertThat(response.body?.get(0)?.name).isEqualTo("청바지")
    }

    @Test
    @DisplayName("상품 상세 조회")
    fun getProductDetail_shouldReturnProductDetail() {
        // given
        val product = Product(name = "청바지", basePrice = 39000).apply { id = 1L }
        val options = listOf(
            ProductOption(product = product, optionName = "M", extraPrice = 1000).apply { id = 1L }
        )
        val productDetailResult = ProductResult.Detail(product, options)

        every { productFacade.getProductDetail(1L) } returns productDetailResult

        // when
        val response = productController.getProductDetail(1L)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.id).isEqualTo(1L)
        assertThat(response.body?.options?.size).isEqualTo(1)
    }

    @Test
    @DisplayName("인기 상품 조회")
    fun getPopularProducts_shouldReturnPopularList() {
        // given
        val condition = ProductCriteria.PeriodCondition(days = 3)
        val popularProducts = listOf(
            ProductResult.Popular(1L, "청바지", 39000, 12),
            ProductResult.Popular(2L, "후드티", 29000, 8)
        )
        every { productFacade.getPopularProducts(condition) } returns popularProducts

        // when
        val response = productController.getPopularProducts(3, null, null)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.size).isEqualTo(2)
        assertThat(response.body?.get(0)?.name).isEqualTo("청바지")
    }
}
