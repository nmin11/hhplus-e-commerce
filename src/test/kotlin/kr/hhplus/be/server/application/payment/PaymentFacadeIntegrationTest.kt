package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceRepository
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItemInfo
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.domain.product.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentFacadeIntegrationTest @Autowired constructor(
    private val paymentFacade: PaymentFacade,
    private val customerRepository: CustomerRepository,
    private val balanceRepository: BalanceRepository,
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val stockRepository: StockRepository,
    private val couponRepository: CouponRepository,
    private val customerCouponRepository: CustomerCouponRepository,
    private val orderRepository: OrderRepository
) {
    private lateinit var customer: Customer
    private lateinit var product: Product
    private lateinit var option: ProductOption
    private lateinit var coupon: Coupon
    private var orderId: Long = 0L

    @BeforeEach
    fun setup() {
        customer = Customer.create("payment-user")
        customerRepository.save(customer)

        val balance = Balance.create(customer, 200_000)
        balanceRepository.save(balance)

        product = Product.create("후드", 50_000)
        productRepository.save(product)

        option = ProductOption.create(product, "L", 5_000)
        productOptionRepository.save(option)

        val stock = Stock.create(option, 10)
        stockRepository.save(stock)

        coupon = Coupon.createRateDiscount(
            name = "10%할인",
            rate = 10,
            quantity = 10,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(7)
        )
        couponRepository.save(coupon)

        val customerCoupon = CustomerCoupon.issue(customer, coupon)
        customerCouponRepository.save(customerCoupon)

        // 주문
        val order = Order.createWithItems(customer, listOf(
            OrderItemInfo(option, quantity = 2)
        ))
        orderRepository.save(order)
        orderId = order.id
    }

    @Test
    @DisplayName("쿠폰을 사용한 결제가 정상 처리")
    fun pay_withValidCoupon_shouldSucceed() {
        // given
        val command = PaymentCommand(
            orderId = orderId,
            couponId = coupon.id
        )

        // when
        val result = paymentFacade.pay(command)

        // then
        assertThat(result.discountAmount).isEqualTo(11_000)
        assertThat(result.originalPrice).isEqualTo(110_000)
        assertThat(result.discountedPrice).isEqualTo(99_000)
    }

    @Test
    @DisplayName("쿠폰 없이도 결제 정상 처리")
    fun pay_withoutCoupon_shouldSucceed() {
        // given
        val command = PaymentCommand(
            orderId = orderId,
            couponId = null
        )

        // when
        val result = paymentFacade.pay(command)

        // then
        assertThat(result.originalPrice).isEqualTo(110_000)
        assertThat(result.discountAmount).isEqualTo(0)
        assertThat(result.discountedPrice).isEqualTo(110_000)
    }
}
