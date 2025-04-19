package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {
    override fun save(product: Product): Product {
        return productJpaRepository.save(product)
    }

    override fun findAll(): List<Product> {
        return productJpaRepository.findAll()
    }

    override fun findAllByIds(ids: List<Long>): List<Product> {
        return productJpaRepository.findAllByIdIn(ids)
    }

    override fun findById(id: Long): Product? {
        return productJpaRepository.findById(id).orElse(null)
    }
}
