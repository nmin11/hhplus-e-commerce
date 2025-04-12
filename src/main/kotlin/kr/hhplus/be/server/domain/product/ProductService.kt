package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun getAll(): List<Product> {
        return productRepository.findAll()
    }

    fun getAllByIds(ids: List<Long>): List<Product> {
        return productRepository.findAllByIds(ids)
    }

    fun getById(id: Long): Product {
        return productRepository.findById(id)
            ?: throw IllegalArgumentException("상품 정보가 존재하지 않습니다.")
    }
}
