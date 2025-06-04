package kr.hhplus.be.server.infrastructure.redis.lua

import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class LuaScriptRegistry {
    fun <T> getScript(scriptId: LuaScriptId, returnType: Class<T>): RedisScript<T> {
        val script = ClassPathResource(scriptId.path)
            .inputStream.bufferedReader()
            .use { it.readText() }

        return RedisScript.of(script, returnType)
    }
}
