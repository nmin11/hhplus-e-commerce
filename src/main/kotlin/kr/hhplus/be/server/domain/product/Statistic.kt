package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Statistic(
    val product: Product,
    val salesCount: Int,
) {
    var id: Long? = null
    var soldAt: LocalDateTime = LocalDateTime.now()
}
