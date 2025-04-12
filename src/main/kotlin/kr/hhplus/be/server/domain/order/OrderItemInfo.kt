package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.product.ProductOption

data class OrderItemInfo(
    val option: ProductOption,
    val quantity: Int
)
