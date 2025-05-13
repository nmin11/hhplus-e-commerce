package kr.hhplus.be.server.infrastructure.redis

import java.time.Duration

interface RedisSortedSetRepository {
    fun add(key: String, member: String, score: Double, ttl: Duration?)
    fun incrementScore(key: String, member: String, score: Double)
}
