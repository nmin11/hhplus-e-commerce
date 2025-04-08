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
        val expectedOptions = listOf(
            ProductOption(productId = productId, optionName = "S", extraPrice = 0).apply { id = 1L },
            ProductOption(productId = productId, optionName = "M", extraPrice = 1000).apply { id = 2L },
            ProductOption(productId = productId, optionName = "L", extraPrice = 2000).apply { id = 3L }
        )
        every { productOptionRepository.findAllByProductId(productId) } returns expectedOptions

        // when
        val result = productOptionService.getByProductId(productId)

        // then
        assertThat(result).isEqualTo(expectedOptions)
    }
}
