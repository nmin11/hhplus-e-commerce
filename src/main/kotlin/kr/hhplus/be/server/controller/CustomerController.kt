package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.CustomerApi
import kr.hhplus.be.server.dto.BalanceChargeRequest
import kr.hhplus.be.server.dto.BalanceHistoryResponse
import kr.hhplus.be.server.dto.BalanceResponse
import kr.hhplus.be.server.dto.CustomerCouponResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerController : CustomerApi {
    override fun getBalance(id: Long): ResponseEntity<BalanceResponse> {
        return ResponseEntity.ok(BalanceResponse(id, 100000))
    }

    override fun getBalanceHistories(id: Long): ResponseEntity<List<BalanceHistoryResponse>> {
        val response = listOf(
            BalanceHistoryResponse(
                changeType = "CHARGE",
                changeAmount = 10000,
                totalAmount = 60000,
                createdAt = "2025-04-02T16:31:11.959Z"
            )
        )

        return ResponseEntity.ok(response)
    }

    override fun chargeBalance(id: Long, @RequestBody request: BalanceChargeRequest): ResponseEntity<BalanceResponse> {
        return ResponseEntity.ok(BalanceResponse(id, 150000))
    }

    override fun getCustomerCoupons(id: Long): ResponseEntity<List<CustomerCouponResponse>> {
        val response = listOf(
            CustomerCouponResponse(
                name = "첫 구매 할인",
                discountType = "FIXED",
                discountAmount = 3000,
                status = "ISSUED",
                issuedAt = "2025-04-02T15:00:00Z",
                expiredAt = "2025-04-30T23:59:59Z"
            ),
            CustomerCouponResponse(
                name = "봄맞이 프로모션",
                discountType = "PERCENT",
                discountAmount = 10,
                status =  "USED",
                issuedAt = "2025-03-20T11:30:00Z",
                expiredAt = "2025-04-10T23:59:59Z"
            )
        )

        return ResponseEntity.ok(response)
    }
}
