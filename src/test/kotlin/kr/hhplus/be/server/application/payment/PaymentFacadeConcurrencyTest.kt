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
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponConflictException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
class PaymentFacadeConcurrencyTest @Autowired constructor(
    private val paymentFacade: PaymentFacade,
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val balanceRepository: BalanceRepository,
    private val couponRepository: CouponRepository,
    private val customerCouponRepository: CustomerCouponRepository,
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val stockRepository: StockRepository
) {
    private lateinit var customer: Customer
    private lateinit var order: Order
    private lateinit var product: Product
    private lateinit var option: ProductOption

    @BeforeEach
    fun setup() {
        customer = Customer.create("concurrent-user")
        customerRepository.save(customer)

        val balance = Balance.create(customer, amount = 100_000)
        balanceRepository.save(balance)

        product = Product.create("test-product", 10_000)
        productRepository.save(product)

        option = ProductOption.create(product, "L", 0)
        productOptionRepository.save(option)

        val stock = Stock.create(option, quantity = 10)
        stockRepository.save(stock)

        order = Order.createWithItems(
            customer = customer,
            items = listOf(OrderItemInfo(option, 1))
        )
        orderRepository.save(order)
    }

    @Test
    @DisplayName("동일한 주문에 대해 동시에 결제를 시도하면 1건만 성공하고 예외 발생")
    fun concurrentPayment_shouldCauseRaceCondition() {
        // given
        val numberOfThreads = 3
        val latch = CountDownLatch(numberOfThreads)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        repeat(numberOfThreads) {
            executor.submit {
                try {
                    paymentFacade.pay(PaymentCommand(orderId = order.id, couponId = null))
                } catch (e: Exception) {
                    exceptions.add(e)
                    println("❗예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // then
        assertThat(exceptions.count { it.message?.contains("지금은 결제를 진행할 수 없습니다") == true })
            .isEqualTo(numberOfThreads - 1)
    }

    @Test
    @DisplayName("동일 사용자가 하나의 쿠폰으로 여러 결제를 동시에 시도하면 1건만 성공하고 예외 발생")
    fun concurrentCouponUsage_shouldAllowOnlyOnePayment() {
        // given

        // 1. 하나의 쿠폰 생성 및 발급
        val coupon = couponRepository.save(
            Coupon.createFixedDiscount(
                name = "concurrency-coupon",
                amount = 1_000,
                quantity = 10,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            )
        )
        customerCouponRepository.save(
            CustomerCoupon.issue(customer, coupon)
        )

        // 2. 결제할 주문 3건 생성
        val orders = (1..3).map {
            val order = Order.createWithItems(
                customer,
                listOf(OrderItemInfo(option, quantity = 1))
            )
            orderRepository.save(order)
        }

        // 3. 동시 실행
        val numberOfThreads = 3
        val latch = CountDownLatch(numberOfThreads)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        orders.forEach { order ->
            executor.submit {
                try {
                    paymentFacade.pay(
                        PaymentCommand(
                            orderId = order.id,
                            couponId = coupon.id
                        )
                    )
                } catch (e: Exception) {
                    exceptions.add(e)
                    println("❗예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // then
        assertThat(exceptions.count { it is CustomerCouponConflictException })
            .isEqualTo(numberOfThreads - 1)
    }
}
