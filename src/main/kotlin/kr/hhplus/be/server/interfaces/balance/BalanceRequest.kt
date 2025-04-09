package kr.hhplus.be.server.interfaces.balance

sealed class BalanceRequest {
    data class Charge(
        val customerId: Long,
        val amount: Int
    )
}
