package kr.hhplus.be.server.infrastructure.redis.lua

enum class LuaScriptId(val path: String) {
    COUPON_ISSUE("lua/coupon-issue.lua")
}
