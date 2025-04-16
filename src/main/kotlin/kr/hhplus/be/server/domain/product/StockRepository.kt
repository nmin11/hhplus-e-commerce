package kr.hhplus.be.server.domain.product

interface StockRepository {
    fun save(stock: Stock): Stock
    fun findByProductOptionId(productOptionId: Long): Stock?
}
