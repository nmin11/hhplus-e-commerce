package kr.hhplus.be.server.domain.product

interface StockRepository {
    fun findByProductOptionId(productOptionId: Long): Stock?
}
