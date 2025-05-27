package kr.hhplus.be.server.application.payment

import com.ninjasquad.springmockk.SpykBean
import io.mockk.verifyOrder
import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceRepository
import kr.hhplus.be.server.domain.balance.event.BalanceDeductRequestedEvent
import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.coupon.event.CouponRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUseRequestedEvent
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItemInfo
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreateRequestedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentEventPublisher
import kr.hhplus.be.server.domain.payment.event.PaymentInitiatedEvent
import kr.hhplus.be.server.domain.product.*
import kr.hhplus.be.server.domain.product.event.StatisticRecordRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockDecreaseRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockRollbackRequestedEvent
import kr.hhplus.be.server.support.exception.balance.BalanceInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyUsedException
import kr.hhplus.be.server.support.exception.order.OrderNotPayableException
import kr.hhplus.be.server.support.exception.product.StockInsufficientException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import java.time.LocalDate
import kotlin.jvm.java

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
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
    @Autowired
    lateinit var applicationEvents: ApplicationEvents

    @SpykBean
    lateinit var paymentEventPublisher: PaymentEventPublisher

    @SpykBean(name = "kafkaPaymentEventPublisher")
    lateinit var kafkaPaymentEventPublisher: PaymentEventPublisher

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

        // Event 전송 테스트
        verifyOrder {
            paymentEventPublisher.publish(ofType(PaymentInitiatedEvent::class))
            paymentEventPublisher.publish(ofType(StockDecreaseRequestedEvent::class))
            paymentEventPublisher.publish(ofType(CouponUseRequestedEvent::class))
            paymentEventPublisher.publish(ofType(BalanceDeductRequestedEvent::class))
            paymentEventPublisher.publish(ofType(PaymentCreateRequestedEvent::class))
            paymentEventPublisher.publish(ofType(PaymentCreatedEvent::class))
            paymentEventPublisher.publish(ofType(StatisticRecordRequestedEvent::class))
            kafkaPaymentEventPublisher.publish(ofType(PaymentCompletedEvent::class))
        }
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

        // Event 전송 테스트
        verifyOrder {
            paymentEventPublisher.publish(ofType(PaymentInitiatedEvent::class))
            paymentEventPublisher.publish(ofType(StockDecreaseRequestedEvent::class))
            paymentEventPublisher.publish(ofType(BalanceDeductRequestedEvent::class))
            paymentEventPublisher.publish(ofType(PaymentCreateRequestedEvent::class))
            paymentEventPublisher.publish(ofType(PaymentCreatedEvent::class))
            paymentEventPublisher.publish(ofType(StatisticRecordRequestedEvent::class))
            kafkaPaymentEventPublisher.publish(ofType(PaymentCompletedEvent::class))
        }
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
            .isInstanceOf(StockInsufficientException::class.java)
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

        assertThat(applicationEvents.stream(StockRollbackRequestedEvent::class.java)).hasSize(1)
    }

    @Test
    @DisplayName("잔액이 부족할 경우 결제 실패")
    fun pay_shouldFail_whenBalanceIsInsufficient() {
        // given
        val customer = Customer.create("insufficient-balance-user")
        customerRepository.save(customer)

        balance = Balance.create(customer, 0)  // 잔액 없음
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
            couponId = coupon.id
        )

        // when & then
        assertThatThrownBy { paymentFacade.pay(command) }
            .isInstanceOf(BalanceInsufficientException::class.java)

        assertThat(applicationEvents.stream(CouponRollbackRequestedEvent::class.java)).hasSize(1)
        assertThat(applicationEvents.stream(StockRollbackRequestedEvent::class.java)).hasSize(1)
    }
}
