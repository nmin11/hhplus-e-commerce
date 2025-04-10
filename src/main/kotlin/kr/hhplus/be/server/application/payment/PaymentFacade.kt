package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.application.balance.BalanceFacade
import kr.hhplus.be.server.application.dataplatform.DataPlatformSender
import kr.hhplus.be.server.domain.balance.BalanceChangeType
import kr.hhplus.be.server.domain.balance.BalanceHistory
import kr.hhplus.be.server.domain.balance.BalanceHistoryService
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.product.Statistic
import kr.hhplus.be.server.domain.product.StatisticService
import kr.hhplus.be.server.domain.product.StockService
import org.springframework.stereotype.Component

@Component
class PaymentFacade(
    private val balanceFacade: BalanceFacade,
    private val balanceHistoryService: BalanceHistoryService,
    private val couponService: CouponService,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val statisticService: StatisticService,
    private val stockService: StockService,
    private val dataPlatformSender: DataPlatformSender
) {
    fun pay(orderId: Long, couponId: Long?): Payment {
        // 1. 주문 조회 및 상태 확인
        val order = orderService.getValidOrderForPayment(orderId)
        val customer = order.customer
        val customerId = customer.id ?: throw IllegalStateException("고객 ID가 존재하지 않습니다.")

        // 2. 재고 검증
        order.orderItems.forEach { item ->
            val optionId = item.productOption.id
                ?: throw IllegalStateException("상품 옵션 ID가 존재하지 않습니다.")
            stockService.validate(optionId, item.quantity)
        }

        // 3. 쿠폰 유효성 검사 및 할인 금액 계산
        val discountAmount = couponId?.let {
            couponService.validateAndCalculateDiscount(it, customerId, order.totalPrice)
        } ?: 0

        // 4. 결제 금액 계산
        val originalPrice = order.totalPrice
        val discountedPrice = originalPrice - discountAmount

        // 5. 잔액 차감
        val balance = balanceFacade.deduct(customerId, discountedPrice)

        // 6. 잔액 변경 내역 저장
        val history = BalanceHistory(
            customer = order.customer,
            changeType = BalanceChangeType.USE,
            changeAmount = discountedPrice,
            totalAmount = balance.amount
        )
        balanceHistoryService.create(history)

        // 7. 결제 저장
        val payment = Payment(
            order = order,
            customer = order.customer,
            coupon = couponId?.let { couponService.getById(it) },
            originalPrice = originalPrice,
            discountAmount = discountAmount,
            discountedPrice = discountedPrice
        )
        paymentService.create(payment)

        // 8. 통계 반영
        order.orderItems.forEach { item ->
            val stat = Statistic(
                product = item.productOption.product,
                salesCount = item.quantity
            )
            statisticService.record(stat)
        }

        // 9. 데이터 플랫폼 전송
        dataPlatformSender.send(order)

        return payment
    }
}
