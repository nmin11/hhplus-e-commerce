package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.application.balance.BalanceCommand
import kr.hhplus.be.server.application.balance.BalanceFacade
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BalanceController(
    private val balanceFacade: BalanceFacade
) : BalanceApi {
    override fun getBalance(customerId: Long): ResponseEntity<BalanceResponse.Summary> {
        val result = balanceFacade.getBalance(customerId)
        return ResponseEntity.ok(BalanceResponse.Summary.from(result))
    }

    override fun getBalanceHistories(customerId: Long): ResponseEntity<List<BalanceResponse.History>> {
        val response = balanceFacade.getHistories(customerId).map { BalanceResponse.History.from(it) }
        return ResponseEntity.ok(response)
    }

    override fun chargeBalance(@RequestBody request: BalanceRequest.Charge): ResponseEntity<BalanceResponse.Summary> {
        val command = BalanceCommand.Charge.from(request)
        val result = balanceFacade.charge(command)
        return ResponseEntity.ok(BalanceResponse.Summary.from(result))
    }
}
