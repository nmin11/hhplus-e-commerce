package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.ProductOption
import org.springframework.data.jpa.repository.JpaRepository

interface ProductOptionJpaRepository : JpaRepository<ProductOption, Long> {
    fun findAllByProductId(productId: Long): List<ProductOption>
}
