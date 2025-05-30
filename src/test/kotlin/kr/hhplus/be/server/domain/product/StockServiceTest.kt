package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kr.hhplus.be.server.support.exception.product.StockInsufficientException
import kr.hhplus.be.server.support.exception.product.StockNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StockServiceTest {
    private val stockRepository = mockk<StockRepository>()
    private val stockService = StockService(stockRepository)

    @Nested
    inner class GetByProductOptionId {
        @Test
        @DisplayName("재고 정보가 존재하면 반환")
        fun whenStockExists_shouldReturnStock() {
            // given
            val optionId = 1L
            val stock = Stock.create(productOption = mockk(), quantity = 20)
            every { stockRepository.findByProductOptionId(optionId) } returns stock

            // when
            val result = stockService.getByProductOptionId(optionId)

            // then
            assertThat(result).isEqualTo(stock)
            verify(exactly = 1) { stockRepository.findByProductOptionId(optionId) }
        }

        @Test
        @DisplayName("재고 정보가 존재하지 않으면 예외 발생")
        fun whenStockNotExists_shouldThrowException() {
            // given
            val optionId = 1L
            every { stockRepository.findByProductOptionId(optionId) } returns null

            // when
            val exception = assertThrows<StockNotFoundException> {
                stockService.getByProductOptionId(optionId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(StockNotFoundException::class.java)
        }
    }

    @Nested
    inner class Validate {
        @Test
        @DisplayName("재고 수량이 충분한 경우 예외 없이 통과")
        fun whenStockIsSufficient_shouldPass() {
            // given
            val optionId = 1L
            val stock = Stock.create(productOption = mockk(), quantity = 10)
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
            val exception = assertThrows<StockNotFoundException> {
                stockService.validate(optionId, requiredQuantity = 3)
            }

            // then
            assertThat(exception)
                .isInstanceOf(StockNotFoundException::class.java)
        }

        @Test
        @DisplayName("재고 수량이 부족하면 예외 발생")
        fun whenStockIsInsufficient_shouldThrowException() {
            // given
            val optionId = 1L
            val stock = Stock.create(productOption = mockk(), quantity = 2)
            every { stockRepository.findByProductOptionId(optionId) } returns stock

            // when
            val exception = assertThrows<StockInsufficientException> {
                stockService.validate(optionId, requiredQuantity = 5)
            }

            // then
            assertThat(exception)
                .isInstanceOf(StockInsufficientException::class.java)
        }
    }

    @Nested
    inner class Decrease {
        @Test
        @DisplayName("정상적인 재고 차감이 이루어짐")
        fun decreaseStockSuccessfully() {
            // given
            val optionId = 1L
            val stock = spyk(Stock.create(productOption = mockk(), quantity = 10))
            every { stockRepository.findByProductOptionIdWithLock(optionId) } returns stock
            every { stockRepository.save(stock) } returns stock

            // when
            stockService.decrease(optionId, 3)

            // then
            verify { stock.decrease(3) }
            verify(exactly = 1) { stockRepository.save(stock) }
        }

        @Test
        @DisplayName("재고 정보가 없으면 예외 발생")
        fun whenStockNotFound_shouldThrowException() {
            // given
            val optionId = 1L
            every { stockRepository.findByProductOptionIdWithLock(optionId) } returns null

            // when
            val exception = assertThrows<StockNotFoundException> {
                stockService.decrease(optionId, 1)
            }

            // then
            assertThat(exception)
                .isInstanceOf(StockNotFoundException::class.java)
        }
    }

    @Nested
    inner class Increase {
        @Test
        @DisplayName("정상적인 재고 증가가 이루어짐")
        fun increaseStockSuccessfully() {
            // given
            val optionId = 1L
            val stock = spyk(Stock.create(productOption = mockk(), quantity = 5))
            every { stockRepository.findByProductOptionId(optionId) } returns stock
            every { stockRepository.save(stock) } returns stock

            // when
            stockService.increase(optionId, 3)

            // then
            verify { stock.increase(3) }
            verify(exactly = 1) { stockRepository.save(stock) }
        }

        @Test
        @DisplayName("재고 정보가 없으면 예외 발생")
        fun whenStockNotFound_shouldThrowException() {
            // given
            val optionId = 1L
            every { stockRepository.findByProductOptionId(optionId) } returns null

            // when
            val exception = assertThrows<StockNotFoundException> {
                stockService.increase(optionId, 1)
            }

            // then
            assertThat(exception).isInstanceOf(StockNotFoundException::class.java)
        }
    }
}
