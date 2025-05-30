package kr.hhplus.be.server.domain.product

interface ProductOptionRepository {
    fun save(productOption: ProductOption): ProductOption
    fun findAllByProductId(productId: Long): List<ProductOption>
    fun findById(id: Long): ProductOption?
}
