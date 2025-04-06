package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Product(
    val name: String,
    val basePrice: Int
) {
    val id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
}
