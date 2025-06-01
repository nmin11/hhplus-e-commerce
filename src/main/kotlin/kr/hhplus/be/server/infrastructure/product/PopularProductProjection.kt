package kr.hhplus.be.server.infrastructure.product

interface PopularProductProjection {
    fun getId(): Long
    fun getName(): String
    fun getBasePrice(): Int
    fun getTotalSales(): Int
}
