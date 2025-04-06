package kr.hhplus.be.server.domain.statistic

import java.time.LocalDateTime

class Statistic(
    val productId: Long,
    val salesCount: Int,
) {
    val id: Long? = null
    val soldAt: LocalDateTime = LocalDateTime.now()
}
