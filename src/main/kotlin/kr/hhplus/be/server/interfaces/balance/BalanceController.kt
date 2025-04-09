package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.application.balance.BalanceFacade
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BalanceController(
    private val balanceFacade: BalanceFacade
) : BalanceApi {
    override fun getBalance(customerId: Long): ResponseEntity<BalanceResponse.Summary> {
        val balance = balanceFacade.getBalance(customerId)
        return ResponseEntity.ok(BalanceResponse.Summary.from(balance))
    }

    override fun getBalanceHistories(customerId: Long): ResponseEntity<List<BalanceResponse.History>> {
        val balanceHistories = balanceFacade.getHistories(customerId).map { BalanceResponse.History.from(it) }
        return ResponseEntity.ok(balanceHistories)
    }

    override fun chargeBalance(@RequestBody request: BalanceRequest.Charge): ResponseEntity<BalanceResponse.Summary> {
        val updatedBalance = balanceFacade.charge(request.customerId, request.amount)
        return ResponseEntity.ok(BalanceResponse.Summary.from(updatedBalance))
    }
}
