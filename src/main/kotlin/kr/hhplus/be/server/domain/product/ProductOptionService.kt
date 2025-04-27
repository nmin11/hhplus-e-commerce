package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.support.exception.product.ProductMissingIdException
import kr.hhplus.be.server.support.exception.product.ProductOptionMismatchException
import kr.hhplus.be.server.support.exception.product.ProductOptionNotFoundException
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
            ?: throw ProductOptionNotFoundException()
    }

    fun validateOptionBelongsToProduct(optionId: Long?, productId: Long?) {
        if (optionId == null || productId == null) {
            throw ProductMissingIdException()
        }

        val option = productOptionRepository.findById(optionId)
            ?: throw ProductOptionNotFoundException()
        if (option.product.id != productId) {
            throw ProductOptionMismatchException()
        }
    }
}
