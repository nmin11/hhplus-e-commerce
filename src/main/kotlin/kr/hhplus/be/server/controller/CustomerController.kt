package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.CustomerApi
import kr.hhplus.be.server.dto.BalanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerController : CustomerApi {
    override fun getBalance(id: Long): ResponseEntity<BalanceResponse> {
        return ResponseEntity.ok(BalanceResponse(id, 100000))
    }
}
