package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductFacadeIntegrationTest @Autowired constructor(
    private val productFacade: ProductFacade,
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val statisticRepository: StatisticRepository,
) {
    private lateinit var product: Product
    private lateinit var option1: ProductOption
    private lateinit var option2: ProductOption

    @BeforeAll
    fun setup() {
        product = Product.create("장갑", basePrice = 4_000)
        productRepository.save(product)

        option1 = ProductOption.create(product, "S", 0)
        option2 = ProductOption.create(product, "L", 1_000)

        productOptionRepository.save(option1)
        productOptionRepository.save(option2)
    }

    @Test
    @DisplayName("상품 상세 정보 조회")
    fun getProductDetail_shouldReturnProductAndOptions() {
        // when
        val result = productFacade.getProductDetail(product.id)

        // then
        assertThat(result.product.name).isEqualTo(product.name)
        assertThat(result.options).hasSize(2)
        assertThat(result.options.map { it.optionName }).containsExactlyInAnyOrder("S", "L")
    }

    @Test
    @DisplayName("최근 3일간 가장 인기 있는 상품들을 조회")
    fun getPopularProducts_shouldReturnTop5BasedOnStatistics() {
        // given
        val product2 = Product.create("양말", basePrice = 4_500)
        productRepository.save(product2)

        val stats = listOf(
            Statistic.create(product, Int.MAX_VALUE),
            Statistic.create(product2, Int.MAX_VALUE - 999_999)
        )
        stats.forEach { statisticRepository.save(it) }

        // when
        val condition = ProductCriteria.PeriodCondition(days = 3)
        val result = productFacade.getPopularProducts(condition)

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2)
        assertThat(result.first().productId).isIn(product.id)
        assertThat(result.first().totalSales).isEqualTo(Int.MAX_VALUE)
    }
}