package kr.hhplus.be.server.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisRepositoryImpl(
    private val stringRedisTemplate: StringRedisTemplate
) : RedisRepository {
    override fun setIfAbsent(key: String, value: String, expire: Duration): Boolean {
        val result = stringRedisTemplate
            .opsForValue()
            .setIfAbsent(key, value, expire)

        return result == true
    }

    override fun releaseWithLua(key: String, value: String): Boolean {
        val luaScript = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
        """.trimIndent()

        val script = RedisScript.of(luaScript, Long::class.java)

        val result = stringRedisTemplate.execute(
            script,
            listOf(key),
            value
        )

        return result == 1L
    }
}
