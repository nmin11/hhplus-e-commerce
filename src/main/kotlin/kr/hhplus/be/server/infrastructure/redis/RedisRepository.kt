package kr.hhplus.be.server.infrastructure.redis

import java.time.Duration

interface RedisRepository {
    fun setIfAbsent(key: String, value: String, expire: Duration): Boolean
    fun releaseWithLua(key: String, value: String): Boolean
}
