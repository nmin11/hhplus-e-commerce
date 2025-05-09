package kr.hhplus.be.server.application.product

import com.github.benmanes.caffeine.cache.Cache
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

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductFacadeIntegrationTest @Autowired constructor(
    private val productFacade: ProductFacade,
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val statisticRepository: StatisticRepository,
    private val stringRedisTemplate: StringRedisTemplate,
    private val caffeineCache: Cache<String, String>
) {
    private lateinit var product: Product
    private lateinit var option1: ProductOption
    private lateinit var option2: ProductOption

    companion object {
        private const val CACHE_KEY = "product:popular:3d"
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
        caffeineCache.invalidate(CACHE_KEY)
        stringRedisTemplate.delete(CACHE_KEY)
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

        val condition = ProductCriteria.PeriodCondition(days = 3)

        // when
        val result = productFacade.getPopularProducts(condition)

        // then
        assertThat(result.first().productId).isIn(product.id)
        assertThat(result.first().totalSales).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    @DisplayName("인기 상품 조회 시 캐시 미스 → Redis 및 In-Memory 캐시에 저장")
    fun getPopularProducts_shouldCacheWhenMiss() {
        // given
        val product3 = Product.create("비니", basePrice = 7000)
        productRepository.save(product3)

        val statistic = Statistic.create(product3, 123)
        statisticRepository.save(statistic)

        // when
        val result = productFacade.getPopularProducts(ProductCriteria.PeriodCondition(3))

        // then
        assertThat(result).anyMatch { it.name == "비니" }

        // redis 캐시 확인
        val redisCache = stringRedisTemplate.opsForValue().get("product:popular:3d")
        assertThat(redisCache).isNotNull()
        assertThat(redisCache).contains("비니")
    }

    @Test
    @DisplayName("인기 상품 조회 시 캐시 히트 → DB에 접근하지 않고 캐시 데이터 반환")
    fun getPopularProducts_shouldUseCacheWhenAvailable() {
        // given
        val cachedJson = """
            [{"id": 2021, "name": "코트", "basePrice": 39000, "totalSales": 100}]
        """.trimIndent()
        stringRedisTemplate.opsForValue().set("product:popular:3d", cachedJson)

        // when
        val result = productFacade.getPopularProducts(ProductCriteria.PeriodCondition(3))

        // then
        assertThat(result[0].name).isEqualTo("코트")
        assertThat(result[0].totalSales).isEqualTo(100)
    }
}
