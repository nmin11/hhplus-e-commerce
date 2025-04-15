package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Stock
import org.springframework.data.jpa.repository.JpaRepository

interface StockJpaRepository : JpaRepository<Stock, Long> {
    fun findByProductOptionId(productOptionId: Long): Stock?
}
