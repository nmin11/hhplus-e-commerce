package kr.hhplus.be.server.support.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import org.springframework.stereotype.Component

@Component
class InMemoryCache(
    private val caffeineCache: Cache<String, String>,
    private val objectMapper: ObjectMapper
) {
    fun <T> getList(key: String, clazz: Class<T>): List<T> {
        val json = caffeineCache.getIfPresent(key) ?: return emptyList()
        val javaType = objectMapper.typeFactory.constructCollectionType(List::class.java, clazz)
        return objectMapper.readValue(json, javaType)
    }

    fun <T> put(key: String, value: T) {
        val json = objectMapper.writeValueAsString(value)
        caffeineCache.put(key, json)
    }
}
