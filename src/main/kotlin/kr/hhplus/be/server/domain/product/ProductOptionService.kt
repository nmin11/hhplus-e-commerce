package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service

@Service
class ProductOptionService(
    private val productOptionRepository: ProductOptionRepository
) {
    fun getByProductId(productId: Long): List<ProductOption> {
        return productOptionRepository.findAllByProductId(productId)
    }

    fun getById(id: Long): ProductOption {
        return productOptionRepository.findById(id)
            ?: throw IllegalArgumentException("상품 옵션 정보가 존재하지 않습니다.")
    }
}
