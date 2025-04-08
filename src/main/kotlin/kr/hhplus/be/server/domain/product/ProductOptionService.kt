package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service

@Service
class ProductOptionService(
    private val productOptionRepository: ProductOptionRepository
) {
    fun getByProductId(productId: Long): List<ProductOption> {
        return productOptionRepository.findAllByProductId(productId)
    }
}
