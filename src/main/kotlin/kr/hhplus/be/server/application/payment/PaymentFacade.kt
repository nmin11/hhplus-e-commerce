package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.application.dataplatform.DataPlatformSender
import kr.hhplus.be.server.domain.balance.BalanceHistory
import kr.hhplus.be.server.domain.balance.BalanceHistoryService
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CustomerCouponService
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.product.Statistic
import kr.hhplus.be.server.domain.product.StatisticService
import kr.hhplus.be.server.domain.product.StockService
import kr.hhplus.be.server.support.lock.DistributedLock
import kr.hhplus.be.server.support.lock.LockType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentFacade(
    private val balanceService: BalanceService,
    private val balanceHistoryService: BalanceHistoryService,
    private val couponService: CouponService,
    private val customerCouponService: CustomerCouponService,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val statisticService: StatisticService,
    private val stockService: StockService,
    private val paymentCommandFactory: PaymentCommandFactory,
    private val dataPlatformSender: DataPlatformSender
) {
    @Transactional
    @DistributedLock(resourceName = "orderId", key = "#command.orderId", lockType = LockType.PUBSUB)
    fun pay(command: PaymentCommand): PaymentResult {
        // 1. 주문 조회 및 상태 확인
        val order = orderService.getValidOrderForPayment(command.orderId)
        val customer = order.customer
        val customerId = customer.id

        // 2. 재고 검증 및 차감
        order.orderItems.forEach { item ->
            val optionId = item.productOption.id
            stockService.validate(optionId, item.quantity)
            stockService.decrease(optionId, item.quantity)
        }

        // 3. 쿠폰 유효성 검사 및 할인 금액 계산
        val discountAmount = command.couponId?.let {
            val customerCoupon = customerCouponService.validateIssuedCoupon(customerId, it)
            val coupon = customerCoupon.coupon

            coupon.validatePeriod()
            customerCouponService.markAsUsed(customerCoupon)

            coupon.calculateDiscount(order.totalPrice)
        } ?: 0

        // 4. 결제 금액 계산
        val originalPrice = order.totalPrice
        val discountedPrice = originalPrice - discountAmount

        // 5. 잔액 차감
        val balance = balanceService.deduct(customerId, discountedPrice)

        // 6. 잔액 변경 내역 저장
        val history = BalanceHistory.use(
            customer = order.customer,
            amount = discountedPrice,
            updatedAmount = balance.getAmount()
        )
        balanceHistoryService.create(history)

        // 7. 결제 정보 저장
        val payment = Payment.create(
            order = order,
            customer = order.customer,
            coupon = command.couponId?.let { couponService.getById(it) },
            originalPrice = originalPrice,
            discountAmount = discountAmount
        )
        paymentService.create(payment)
        orderService.markAsPaid(order)

        // 8. 통계 반영
        order.orderItems.forEach { item ->
            val stat = Statistic.create(
                product = item.productOption.product,
                salesCount = item.quantity
            )
            statisticService.record(stat)
        }

        // 9. 데이터 플랫폼 전송
        val orderCommand = paymentCommandFactory.from(order)
        dataPlatformSender.send(orderCommand)

        return PaymentResult.from(payment)
    }
}
