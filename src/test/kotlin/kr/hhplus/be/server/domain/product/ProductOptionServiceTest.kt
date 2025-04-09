package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProductOptionServiceTest {
    private val productOptionRepository = mockk<ProductOptionRepository>()
    private val productOptionService = ProductOptionService(productOptionRepository)

    @Test
    fun getByProductId_shouldReturnProductOptions() {
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
