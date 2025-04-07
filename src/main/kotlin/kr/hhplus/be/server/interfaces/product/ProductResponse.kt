package kr.hhplus.be.server.interfaces.product

sealed class ProductResponse {
    data class Summary(
        val id: Long,
        val name: String,
        val basePrice: Int
    )

    data class Detail(
        val id: Long,
        val name: String,
        val basePrice: Int,
        val options: List<Option>
    )

    data class Option(
        val name: String,
        val extraPrice: Int
    )

    data class Popular(
        val id: Long,
        val name: String,
        val basePrice: Int,
        val salesCount: Int
    )
}
