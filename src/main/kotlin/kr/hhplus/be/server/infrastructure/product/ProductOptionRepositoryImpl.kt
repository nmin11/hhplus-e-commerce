package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.ProductOption
import kr.hhplus.be.server.domain.product.ProductOptionRepository
import org.springframework.stereotype.Repository

@Repository
class ProductOptionRepositoryImpl(
    private val productOptionJpaRepository: ProductOptionJpaRepository
) : ProductOptionRepository {
    override fun save(productOption: ProductOption): ProductOption {
        return productOptionJpaRepository.save(productOption)
    }

    override fun findAllByProductId(productId: Long): List<ProductOption> {
        return productOptionJpaRepository.findAllByProductId(productId)
    }

    override fun findById(id: Long): ProductOption? {
        return productOptionJpaRepository.findById(id).orElse(null)
    }
}
