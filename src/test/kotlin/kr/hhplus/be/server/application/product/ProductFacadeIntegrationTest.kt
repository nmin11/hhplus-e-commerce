package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductFacadeIntegrationTest @Autowired constructor(
    private val productFacade: ProductFacade,
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val statisticRepository: StatisticRepository,
    private val stringRedisTemplate: StringRedisTemplate
) {
    private lateinit var product: Product
    private lateinit var option1: ProductOption
    private lateinit var option2: ProductOption

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val today = LocalDate.now()
        private val REDIS_KEY = "product:rank:3d:${today.format(dateFormatter)}"
    }

    @BeforeAll
    fun setup() {
        product = Product.create("장갑", basePrice = 4_000)
        productRepository.save(product)

        option1 = ProductOption.create(product, "S", 0)
        option2 = ProductOption.create(product, "L", 1_000)

        productOptionRepository.save(option1)
        productOptionRepository.save(option2)
    }

    @BeforeEach
    fun clearCache() {
        stringRedisTemplate.delete(REDIS_KEY)
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
    fun getPopularProducts_shouldReturnTop5Products() {
        // given
        val thirdPlaceProduct = Product.create("C", basePrice = 1000)
        val firstPlaceProduct = Product.create("A", basePrice = 1000)
        val secondPlaceProduct = Product.create("B", basePrice = 1000)
        listOf(thirdPlaceProduct, firstPlaceProduct, secondPlaceProduct).forEach { productRepository.save(it) }

        val key = "product:rank:3d:${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
        val zSetOps = stringRedisTemplate.opsForZSet()
        zSetOps.add(key, thirdPlaceProduct.id.toString(), 300.0)
        zSetOps.add(key, firstPlaceProduct.id.toString(), 500.0)
        zSetOps.add(key, secondPlaceProduct.id.toString(), 400.0)

        // when
        val result = productFacade.getPopularProducts(ProductCriteria.PeriodCondition(days = 3))

        // then
        assertThat(result.map { it.productId }).containsExactly(
            firstPlaceProduct.id, secondPlaceProduct.id, thirdPlaceProduct.id
        )
    }

    @Test
    @DisplayName("7일 초과 범위 요청 시 DB 조회 후 Redis 저장")
    fun getPopularProducts_shouldQueryDatabaseAndSaveToRedisWhenZSetIsMissing() {
        // given
        val product2 = Product.create("롱패딩", basePrice = 120_000)
        productRepository.save(product2)

        val statistic = Statistic.create(product2, Int.MAX_VALUE)
        statisticRepository.save(statistic)

        val condition = ProductCriteria.PeriodCondition(days = 10)
        val redisKey = "product:rank:10d:${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
        stringRedisTemplate.delete(redisKey)

        // when
        val result = productFacade.getPopularProducts(condition)

        // then
        assertThat(result.first().name).isEqualTo("롱패딩")
        assertThat(result.first().totalSales).isEqualTo(Int.MAX_VALUE)
        assertThat(stringRedisTemplate.hasKey(redisKey)).isTrue()
    }
}
