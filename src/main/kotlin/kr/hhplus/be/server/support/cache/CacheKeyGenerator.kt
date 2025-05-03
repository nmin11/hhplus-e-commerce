package kr.hhplus.be.server.support.cache

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CacheKeyGenerator {
    fun generate(resourceName: String, displayName: String, since: LocalDate?): String {
        val base = "$resourceName:$displayName"
        return since?.let {
            val days = ChronoUnit.DAYS.between(it, LocalDate.now())
            "$base:${days}d"
        } ?: base
    }
}
