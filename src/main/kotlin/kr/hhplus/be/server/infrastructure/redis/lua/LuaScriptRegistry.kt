package kr.hhplus.be.server.infrastructure.redis.lua

import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils

@Component
class LuaScriptRegistry {
    fun <T> getScript(scriptId: LuaScriptId, returnType: Class<T>): RedisScript<T> {
        val script = ResourceUtils.getFile("classpath:${scriptId.path}").readText()
        return RedisScript.of(script, returnType)
    }
}
