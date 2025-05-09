package kr.hhplus.be.server.support.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryCacheTest {
    private lateinit var caffeineCache: Cache<String, String>
    private lateinit var objectMapper: ObjectMapper
    private lateinit var inMemoryCache: InMemoryCache

    @BeforeEach
    fun setUp() {
        caffeineCache = mockk()
        objectMapper = ObjectMapper()
        inMemoryCache = InMemoryCache(caffeineCache, objectMapper)
    }

    @Nested
    inner class GetList {
        @Test
        @DisplayName("존재하는 캐시 데이터를 리스트로 역직렬화하여 반환")
        fun getList_shouldReturnDeserializedList() {
            // given
            val key = "test-key"
            val list = listOf("item1", "item2")
            val json = objectMapper.writeValueAsString(list)

            every { caffeineCache.getIfPresent(key) } returns json

            // when
            val result = inMemoryCache.getList(key, String::class.java)

            // then
            assertThat(result).containsExactly("item1", "item2")
        }

        @Test
        @DisplayName("존재하지 않는 캐시 데이터는 빈 리스트 반환")
        fun getList_shouldReturnEmptyListIfNotFound() {
            // given
            every { caffeineCache.getIfPresent("missing-key") } returns null

            // when
            val result = inMemoryCache.getList("missing-key", String::class.java)

            // then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class Put {
        @Test
        @DisplayName("객체를 JSON 문자열로 직렬화하여 캐시에 저장")
        fun put_shouldSerializeAndStoreInCache() {
            // given
            val key = "put-key"
            val value = listOf("value1", "value2")
            val json = objectMapper.writeValueAsString(value)

            every { caffeineCache.put(key, json) } just Runs

            // when
            inMemoryCache.put(key, value)

            // then
            verify { caffeineCache.put(key, json) }
        }
    }
}
