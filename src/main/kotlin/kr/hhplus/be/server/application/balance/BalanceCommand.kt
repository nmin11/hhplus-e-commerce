package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.interfaces.balance.BalanceRequest

sealed class BalanceCommand {
    data class Charge(
        val customerId: Long,
        val amount: Int
    ) {
        companion object {
            fun from(request: BalanceRequest.Charge): Charge {
                return Charge(
                    customerId = request.customerId,
                    amount = request.amount
                )
            }
        }
    }
}
