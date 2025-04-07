package kr.hhplus.be.server.interfaces.customer

import kr.hhplus.be.server.application.balance.BalanceFacade
import kr.hhplus.be.server.interfaces.balance.BalanceRequest
import kr.hhplus.be.server.interfaces.balance.BalanceResponse
import kr.hhplus.be.server.interfaces.coupon.CouponResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerController(
    private val balanceFacade: BalanceFacade
) : CustomerApi {
    override fun getBalance(id: Long): ResponseEntity<BalanceResponse.Summary> {
        val balance = balanceFacade.getByCustomerId(id)
        return ResponseEntity.ok(BalanceResponse.from(balance))
    }

    override fun getBalanceHistories(id: Long): ResponseEntity<List<BalanceResponse.Histories>> {
        val response = listOf(
            BalanceResponse.Histories(
                changeType = "CHARGE",
                changeAmount = 10000,
                totalAmount = 60000,
                createdAt = "2025-04-02T16:31:11.959Z"
            )
        )

        return ResponseEntity.ok(response)
    }

    override fun chargeBalance(
        id: Long,
        @RequestBody request: BalanceRequest.Charge
    ): ResponseEntity<BalanceResponse.Summary> {
        return ResponseEntity.ok(BalanceResponse.Summary(id, 150000))
    }

    override fun getCustomerCoupons(id: Long): ResponseEntity<List<CouponResponse.Owned>> {
        val response = listOf(
            CouponResponse.Owned(
                name = "첫 구매 할인",
                discountType = "FIXED",
                discountAmount = 3000,
                status = "ISSUED",
                issuedAt = "2025-04-02T15:00:00Z",
                expiredAt = "2025-04-30T23:59:59Z"
            ),
            CouponResponse.Owned(
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
