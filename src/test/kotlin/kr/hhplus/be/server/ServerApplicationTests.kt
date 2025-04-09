package kr.hhplus.be.server

import com.ninjasquad.springmockk.MockkBean
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository
import kr.hhplus.be.server.domain.balance.BalanceRepository
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.domain.payment.PaymentRepository
import kr.hhplus.be.server.domain.product.ProductOptionRepository
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.product.StatisticRepository
import kr.hhplus.be.server.domain.product.StockRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ServerApplicationTests {
    @MockkBean
    lateinit var customerRepository: CustomerRepository

    @MockkBean
    lateinit var balanceRepository: BalanceRepository

    @MockkBean
    lateinit var balanceHistoryRepository: BalanceHistoryRepository

    @MockkBean
    lateinit var productRepository: ProductRepository

    @MockkBean
    lateinit var productOptionRepository: ProductOptionRepository

    @MockkBean
    lateinit var statisticRepository: StatisticRepository

    @MockkBean
    lateinit var stockRepository: StockRepository

    @MockkBean
    lateinit var orderRepository: OrderRepository

    @MockkBean
    lateinit var paymentRepository: PaymentRepository

    @MockkBean
    lateinit var couponRepository: CouponRepository

    @MockkBean
    lateinit var customerCouponRepository: CustomerCouponRepository

	@Test
	fun contextLoads() {}

}
