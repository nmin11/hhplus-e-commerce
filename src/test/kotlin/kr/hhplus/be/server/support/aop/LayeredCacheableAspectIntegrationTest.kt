package kr.hhplus.be.server.support.aop

import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.support.cache.InMemoryCache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class LayeredCacheableAspectIntegrationTest {
    @Autowired
    lateinit var dummyCachedService: CacheExampleService

    @Autowired
    lateinit var inMemoryCache: InMemoryCache

    @Autowired
    lateinit var redisRepository: RedisRepository

    val key = "dummy:test:3d"
    val since: LocalDate = LocalDate.now().minusDays(3)

    @BeforeEach
    fun clearCaches() {
        inMemoryCache.put(key, emptyList<DummyData>())
        redisRepository.save(key, emptyList<DummyData>(), Duration.ofSeconds(1))
    }

    @Test
    @DisplayName("캐시 미스 시 DB 조회 후 InMemory, Redis 캐시에 저장")
    fun cacheMiss_shouldStoreToCaches() {
        // when
        val result = dummyCachedService.getDummyList(since)

        // then
        assertThat(result).hasSize(2)

        val memory = inMemoryCache.getList(key, DummyData::class.java)
        val redis = redisRepository.findList(key, DummyData::class.java)

        assertThat(memory).isNotEmpty
        assertThat(redis).isNotEmpty
        assertThat(memory.map { it.name }).containsExactly("A", "B")
    }

    @Test
    @DisplayName("InMemory Cache Hit")
    fun inMemoryCacheHit_shouldNotCallMethodAgain() {
        // given
        val dummyList = listOf(DummyData("X", 100))
        inMemoryCache.put(key, dummyList)

        // when
        val result = dummyCachedService.getDummyList(since)

        // then
        assertThat(result.map { it.name }).containsExactly("X")
    }

    @Test
    @DisplayName("Redis Cache Hit → InMemory 저장")
    fun redisCacheHit_shouldUpdateMemory() {
        // given
        val dummyList = listOf(DummyData("Z", 999))
        redisRepository.save(key, dummyList, Duration.ofSeconds(60))

        // when
        val result = dummyCachedService.getDummyList(since)

        // then
        assertThat(result.map { it.name }).containsExactly("Z")

        val memory = inMemoryCache.getList(key, DummyData::class.java)
        assertThat(memory).isNotEmpty
    }
}
