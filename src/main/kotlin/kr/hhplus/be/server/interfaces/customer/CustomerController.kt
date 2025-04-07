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
        val balance = balanceFacade.getBalance(id)
        return ResponseEntity.ok(BalanceResponse.from(balance))
    }

    override fun getBalanceHistories(id: Long): ResponseEntity<List<BalanceResponse.History>> {
        val balanceHistories = balanceFacade.getHistories(id).map { BalanceResponse.from(it) }
        return ResponseEntity.ok(balanceHistories)
    }

    override fun chargeBalance(
        id: Long,
        @RequestBody request: BalanceRequest.Charge
    ): ResponseEntity<BalanceResponse.Summary> {
        val updatedBalance = balanceFacade.charge(id, request.amount)
        return ResponseEntity.ok(BalanceResponse.from(updatedBalance))
    }

    /* TODO STEP 6 쿠폰 기능 구현 시 수정 */
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
