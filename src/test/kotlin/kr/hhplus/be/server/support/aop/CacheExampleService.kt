package kr.hhplus.be.server.support.aop

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CacheExampleService {
    @LayeredCacheable(resourceName = "dummy", displayName = "test", redisTtlSeconds = 60)
    fun getDummyList(since: LocalDate): List<DummyData> {
        return listOf(DummyData("A", 1), DummyData("B", 2))
    }
}

data class DummyData(val name: String, val value: Int)
