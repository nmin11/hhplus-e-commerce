package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Statistic(
    val productId: Long,
    val salesCount: Int,
) {
    var id: Long? = null
    val soldAt: LocalDateTime = LocalDateTime.now()
}
