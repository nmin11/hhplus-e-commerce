package kr.hhplus.be.server.domain.product

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
class StockServiceConcurrencyTest @Autowired constructor(
    private val stockService: StockService,
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val stockRepository: StockRepository
) {
    private lateinit var option: ProductOption
    private lateinit var stock: Stock

    @BeforeEach
    fun setup() {
        val product = Product.create("insufficient-product", 5000)
        productRepository.save(product)

        option = ProductOption.create(product, "insufficient-option", 0)
        productOptionRepository.save(option)

        stock = Stock.create(option, 3)
        stockRepository.save(stock)
    }

    @Test
    @DisplayName("재고가 음수가 되도록 하는 재고 차감 요청 동시성 테스트")
    fun concurrentDecrease_shouldCauseRaceCondition() {
        // given
        val numberOfThreads = 10
        val latch = CountDownLatch(numberOfThreads)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        repeat(numberOfThreads) {
            executor.submit {
                try {
                    stockService.decrease(option.id, 1)
                } catch (e: Exception) {
                    exceptions.add(e)
                    println("❗예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // then
        val remainingStock = stockService.getByProductOptionId(option.id).quantity
        println("📦 최종 재고 수량: $remainingStock")

        assertThat(remainingStock).isEqualTo(0)
        assertThat(exceptions.count { it.message?.contains("재고가 부족합니다") == true })
            .isGreaterThan(0)
    }
}
