package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.CustomerApi
import kr.hhplus.be.server.dto.BalanceChargeRequest
import kr.hhplus.be.server.dto.BalanceHistoryResponse
import kr.hhplus.be.server.dto.BalanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerController : CustomerApi {
    override fun getBalance(id: Long): ResponseEntity<BalanceResponse> {
        return ResponseEntity.ok(BalanceResponse(id, 100000))
    }

    override fun getBalanceHistories(id: Long): ResponseEntity<List<BalanceHistoryResponse>> {
        return ResponseEntity.ok(listOf(
            BalanceHistoryResponse(
                changeType = "CHARGE",
                changeAmount = 10000,
                totalAmount = 60000,
                createdAt = "2025-04-02T16:31:11.959Z"
            )
        ))
    }

    override fun chargeBalance(id: Long, request: BalanceChargeRequest): ResponseEntity<BalanceResponse> {
        return ResponseEntity.ok(BalanceResponse(id, 150000))
    }
}
