package kr.hhplus.be.server.interfaces.dto.response

import kr.hhplus.be.server.domain.balance.Balance

data class BalanceResponse(
    val customerId: Long,
    val amount: Int
) {
    companion object {
        fun from(balance: Balance) = BalanceResponse(
            customerId = balance.customerId,
            amount = balance.amount
        )
    }
}
