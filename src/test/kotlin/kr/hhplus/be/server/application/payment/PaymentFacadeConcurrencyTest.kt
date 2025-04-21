package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceRepository
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
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles
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
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val stockRepository: StockRepository
) {
    private lateinit var customer: Customer
    private lateinit var order: Order

    @BeforeEach
    fun setup() {
        customer = Customer.create("concurrent-user")
        customerRepository.save(customer)

        val balance = Balance.create(customer, amount = 200_000)
        balanceRepository.save(balance)

        val product = Product.create("test-product", 100_000)
        productRepository.save(product)

        val option = ProductOption.create(product, "L", 0)
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
    @DisplayName("동일한 주문에 대해 동시에 결제를 시도하면 예외 발생")
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
        assertThat(exceptions.count { it is ObjectOptimisticLockingFailureException })
            .isEqualTo(numberOfThreads - 1)
    }
}
