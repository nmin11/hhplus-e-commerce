package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Product(
    val name: String,
    val basePrice: Int
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
    var productOptions: MutableList<ProductOption> = mutableListOf()
    var statistics: MutableList<Statistic> = mutableListOf()
}
