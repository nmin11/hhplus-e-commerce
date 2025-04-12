package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StockServiceTest {
    private val stockRepository = mockk<StockRepository>()
    private val stockService = StockService(stockRepository)

    @Test
    @DisplayName("재고가 충분하면 예외 없이 통과")
    fun whenStockIsSufficient_shouldPass() {
        // given
        val optionId = 1L
        val stock = Stock.create(productOption = mockk(), quantity = 10).apply { id = 1L }
        every { stockRepository.findByProductOptionId(optionId) } returns stock

        // when & then
        stockService.validate(optionId, requiredQuantity = 5)
    }

    @Test
    @DisplayName("재고 정보가 없으면 예외 발생")
    fun whenStockNotFound_shouldThrowException() {
        // given
        val optionId = 1L
        every { stockRepository.findByProductOptionId(optionId) } returns null

        // when
        val exception = assertThrows<IllegalArgumentException> {
            stockService.validate(optionId, requiredQuantity = 3)
        }

        // then
        assertThat(exception)
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("재고 정보가 존재하지 않습니다.")
    }

    @Test
    @DisplayName("재고 수량이 부족하면 예외 발생")
    fun whenStockIsInsufficient_shouldThrowException() {
        // given
        val optionId = 1L
        val stock = Stock.create(productOption = mockk(), quantity = 2).apply { id = 1L }
        every { stockRepository.findByProductOptionId(optionId) } returns stock

        // when
        val exception = assertThrows<IllegalStateException> {
            stockService.validate(optionId, requiredQuantity = 5)
        }

        // then
        assertThat(exception)
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("재고가 2개 남아 있어서 주문이 불가능합니다.")
    }
}
