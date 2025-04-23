package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceRepository
import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItemInfo
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.domain.product.*
import kr.hhplus.be.server.support.exception.balance.BalanceInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyUsedException
import kr.hhplus.be.server.support.exception.order.OrderNotPayableException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private lateinit var product: Product
    private lateinit var option: ProductOption
    private lateinit var stock: Stock
    private lateinit var balance: Balance

    @BeforeAll
    fun setup() {
        product = Product.create("후드", 50_000)
        productRepository.save(product)

        option = ProductOption.create(product, "L", 5_000)
        productOptionRepository.save(option)

        stock = Stock.create(option, 100)
        stockRepository.save(stock)
    }

    @Test
    @DisplayName("쿠폰을 사용한 결제의 정상 처리")
    fun pay_withValidCoupon_shouldSucceed() {
        // given
        val customer = Customer.create("coupon-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, 200_000)
        balanceRepository.save(balance)

        val coupon = Coupon.createRateDiscount(
            name = "10-percent-discount",
            rate = 10,
            quantity = 10,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(7)
        )
        couponRepository.save(coupon)

        val customerCoupon = CustomerCoupon.issue(customer, coupon)
        customerCouponRepository.save(customerCoupon)

        val order = Order.createWithItems(customer, listOf(
            OrderItemInfo(option, quantity = 2)
        ))
        orderRepository.save(order)

        val command = PaymentCommand(
            orderId = order.id,
            couponId = coupon.id // 사용할 쿠폰 ID
        )

        // when
        val result = paymentFacade.pay(command)

        // then
        assertThat(result.originalPrice).isEqualTo(110_000)
        assertThat(result.discountAmount).isEqualTo(11_000)
        assertThat(result.discountedPrice).isEqualTo(99_000)
    }

    @Test
    @DisplayName("쿠폰을 사용하지 않은 결제의 정상 처리")
    fun pay_withoutCoupon_shouldSucceed() {
        // given
        val customer = Customer.create("without-coupon-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, 200_000)
        balanceRepository.save(balance)

        val order = Order.createWithItems(customer, listOf(
            OrderItemInfo(option, quantity = 2)
        ))
        orderRepository.save(order)

        val command = PaymentCommand(
            orderId = order.id,
            couponId = null // 사용할 쿠폰 ID가 없음
        )

        // when
        val result = paymentFacade.pay(command)

        // then
        assertThat(result.originalPrice).isEqualTo(110_000)
        assertThat(result.discountAmount).isEqualTo(0)
        assertThat(result.discountedPrice).isEqualTo(110_000)
    }

    @Test
    @DisplayName("결제 시 주문 상태가 CREATED 가 아니면 예외 발생")
    fun pay_shouldFail_whenOrderStatusIsNotCreated() {
        // given
        val customer = Customer.create("already-ordered-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, 200_000)
        balanceRepository.save(balance)

        val order = Order.createWithItems(customer, listOf(
            OrderItemInfo(option, quantity = 2)
        ))
        order.markAsPaid() // 결제된 주문으로 처리
        orderRepository.save(order)

        val command = PaymentCommand(
            orderId = order.id,
            couponId = null
        )

        // when & then
        assertThatThrownBy {
            paymentFacade.pay(command)
        }.isInstanceOf(OrderNotPayableException::class.java)
    }

    @Test
    @DisplayName("결제 시 상품 재고가 부족하면 예외 발생")
    fun pay_shouldFail_whenStockIsInsufficient() {
        // given
        val customer = Customer.create("without-coupon-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, 200_000)
        balanceRepository.save(balance)

        val insufficientOption = ProductOption.create(product, "insufficient-option", 0)
        productOptionRepository.save(insufficientOption)

        val insufficientStock = Stock.create(insufficientOption, 0)
        stockRepository.save(insufficientStock)

        val order = Order.createWithItems(customer, listOf(
            OrderItemInfo(insufficientOption, quantity = 2)
        ))
        orderRepository.save(order)

        val command = PaymentCommand(
            orderId = order.id,
            couponId = null
        )

        // when & then
        assertThatThrownBy { paymentFacade.pay(command) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("재고가 0개 남아 있어서 주문이 불가능합니다.")
    }

    @Test
    @DisplayName("이미 사용된 쿠폰으로 결제 시 예외 발생")
    fun pay_shouldFail_withUsedCoupon() {
        // given
        val customer = Customer.create("used-coupon-user")
        customerRepository.save(customer)

        val order = Order.createWithItems(customer, listOf(
            OrderItemInfo(option, quantity = 2)
        ))
        orderRepository.save(order)

        val usedCoupon = Coupon.createFixedDiscount(
            name = "used-coupon",
            amount = 1000,
            quantity = 10,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(7)
        )
        val usedCustomerCoupon = CustomerCoupon.issue(customer, usedCoupon)
        usedCustomerCoupon.status = CustomerCouponStatus.USED // 사용된 쿠폰 처리

        couponRepository.save(usedCoupon)
        customerCouponRepository.save(usedCustomerCoupon)

        val command = PaymentCommand(
            orderId = order.id,
            couponId = usedCoupon.id
        )

        // when & then
        assertThatThrownBy { paymentFacade.pay(command) }
            .isInstanceOf(CustomerCouponAlreadyUsedException::class.java)
    }

    @Test
    @DisplayName("잔액이 부족할 경우 결제 실패")
    fun pay_shouldFail_whenBalanceIsInsufficient() {
        // given
        val customer = Customer.create("insufficient-balance-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, 0)  // 잔액 없음
        balanceRepository.save(balance)

        val order = Order.createWithItems(customer, listOf(
            OrderItemInfo(option, quantity = 2)
        ))
        orderRepository.save(order)

        val command = PaymentCommand(
            orderId = order.id,
            couponId = null
        )

        // when & then
        assertThatThrownBy { paymentFacade.pay(command) }
            .isInstanceOf(BalanceInsufficientException::class.java)
    }
}
