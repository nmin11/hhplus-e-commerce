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

    fun validateOptionBelongsToProduct(optionId: Long?, productId: Long?) {
        if (optionId == null || productId == null) {
            throw IllegalArgumentException("상품 ID 또는 상품 옵션 ID가 존재하지 않습니다.")
        }

        val option = productOptionRepository.findById(optionId)
            ?: throw IllegalArgumentException("상품 옵션이 존재하지 않습니다.")
        if (option.product.id != productId) {
            throw IllegalStateException("상품 옵션이 해당 상품에 속하지 않습니다.")
        }
    }
}
