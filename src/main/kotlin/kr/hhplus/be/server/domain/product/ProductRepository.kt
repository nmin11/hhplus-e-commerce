package kr.hhplus.be.server.domain.product

interface ProductRepository {
    fun save(product: Product): Product
    fun findAll(): List<Product>
    fun findAllByIds(ids: List<Long>): List<Product>
    fun findById(id: Long): Product?
}
