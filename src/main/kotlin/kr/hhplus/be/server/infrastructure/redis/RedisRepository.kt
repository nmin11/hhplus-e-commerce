package kr.hhplus.be.server.infrastructure.redis

import org.springframework.data.redis.core.script.RedisScript
import java.time.Duration

interface RedisRepository {
    fun exists(key: String): Boolean
    fun setIfAbsent(key: String, value: String, expire: Duration): Boolean
    fun <T> executeWithLua(script: RedisScript<T>, keys: List<String>, args: List<String>): T?
    fun releaseWithLua(key: String, value: String): Boolean
    fun <T> save(key: String, value: T, ttl: Duration)
    fun <T> findList(key: String, clazz: Class<T>): List<T>
}
