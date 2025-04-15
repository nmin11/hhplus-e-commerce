package kr.hhplus.be.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.spyk
import kr.hhplus.be.server.domain.balance.*
import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentRepository
import kr.hhplus.be.server.domain.product.*
import kr.hhplus.be.server.interfaces.coupon.CouponRequest
import kr.hhplus.be.server.interfaces.order.OrderRequest
import kr.hhplus.be.server.interfaces.payment.PaymentRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class ApiE2ETest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

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
    @DisplayName("전체 API 성공 흐름 테스트")
    fun allApiSuccessFlow() {
        val now = LocalDateTime.now()

        val customer = spyk(Customer.create(username = "tester"))

        every { customer.id } returns 1L

        every { customerRepository.existsById(1L) } returns true

        every { customerRepository.findById(1L) } returns customer

        val balance = Balance.create(customer = customer, amount = 150000)
        every { balanceRepository.findByCustomerId(1L) } returns balance

        val history = BalanceHistory.charge(
            customer = customer,
            amount = 1000,
            updatedAmount = 6000
        )
        history.createdAt = now

        every { balanceHistoryRepository.findAllByCustomerId(1L) } returns listOf(history)

        every { balanceRepository.save(any()) } answers { firstArg() }
        every { balanceHistoryRepository.save(any()) } answers { firstArg() }

        val product1 = Product.create(name = "청바지", basePrice = 39000)
        val product2 = Product.create(name = "후드티", basePrice = 29000)
        val product3 = Product.create(name = "운동화", basePrice = 59000)
        val product4 = Product.create(name = "잠바", basePrice = 79000)
        val product5 = Product.create(name = "실내화", basePrice = 15000)

        every { productRepository.findAll() } returns listOf(product1, product2, product3)
        every { productRepository.findById(1L) } returns product1
        every { productRepository.findById(2L) } returns product2

        every { productOptionRepository.findAllByProductId(1L) } returns listOf(
            ProductOption.create(product = product1, optionName = "S", extraPrice = 0),
            ProductOption.create(product = product1, optionName = "M", extraPrice = 1000),
            ProductOption.create(product = product1, optionName = "L", extraPrice = 2000)
        )

        every {
            productRepository.findAllByIds(listOf(1L, 2L, 3L, 4L, 5L))
        } returns listOf(product1, product2, product3, product4, product5)

        val stat1 = spyk(Statistic.create(product = product1, salesCount = 12))
        val stat2 = spyk(Statistic.create(product = product2, salesCount = 9))
        val stat3 = spyk(Statistic.create(product = product3, salesCount = 7))
        val stat4 = spyk(Statistic.create(product = product4, salesCount = 5))
        val stat5 = spyk(Statistic.create(product = product5, salesCount = 3))

        every { stat1.soldAt } returns LocalDateTime.now().minusDays(1)
        every { stat2.soldAt } returns LocalDateTime.now().minusDays(2)
        every { stat3.soldAt } returns LocalDateTime.now().minusDays(3)
        every { stat4.soldAt } returns LocalDateTime.now().minusDays(1)
        every { stat5.soldAt } returns LocalDateTime.now().minusDays(2)

        every {
            statisticRepository.findTop5BySoldAtAfterOrderBySalesCountDesc(any())
        } returns listOf(stat1, stat2, stat3, stat4, stat5)

        val optionM = spyk(ProductOption.create(product = product1, optionName = "M", extraPrice = 1000))
        val optionL = spyk(ProductOption.create(product = product2, optionName = "L", extraPrice = 2000))

        val optionMId = 2L
        val optionLId = 3L

        every { optionM.id } returns optionMId
        every { optionL.id } returns optionLId

        every { productOptionRepository.findById(optionMId) } returns optionM
        every { productOptionRepository.findById(optionLId) } returns optionL

        val stockOptionM = Stock.create(productOption = optionM, quantity = 10)
        val stockOptionL = Stock.create(productOption = optionL, quantity = 10)

        every { stockRepository.findByProductOptionId(optionMId) } returns stockOptionM
        every { stockRepository.findByProductOptionId(optionLId) } returns stockOptionL

        val order = Order.create(
            customer = customer
        ).apply {
            totalPrice = 40000 + 31000
            orderItems.addAll(
                listOf(
                    OrderItem(order = this, productOption = optionM, quantity = 1, subtotalPrice = 40000),
                    OrderItem(order = this, productOption = optionL, quantity = 1, subtotalPrice = 31000)
                )
            )
            createdAt = now
        }

        every { orderRepository.save(any()) } returns order

        every { orderRepository.findById(1L) } returns order

        every { statisticRepository.save(any()) } answers { firstArg() }

        every { paymentRepository.save(any()) } answers {
            val payment = firstArg<Payment>()
            payment
        }

        val coupon = Coupon.createFixedDiscount(
            name = "5천원 할인 쿠폰",
            amount = 5000,
            quantity = 100,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(1)
        )

        every { couponRepository.findById(1L) } returns coupon

        every { customerCouponRepository.findByCustomerIdAndCouponId(1L, 1L) } returns null

        every { couponRepository.save(any()) } answers { firstArg() }

        every { customerCouponRepository.save(any()) } answers { firstArg() }

        val coupon1 = Coupon.createFixedDiscount(
            name = "첫 구매 할인",
            amount = 3000,
            quantity = 100,
            startedAt = LocalDate.parse("2025-04-01"),
            expiredAt = LocalDate.parse("2025-04-30")
        )

        val coupon2 = Coupon.createRateDiscount(
            name = "봄맞이 프로모션",
            rate = 10,
            quantity = 50,
            startedAt = LocalDate.parse("2025-03-15"),
            expiredAt = LocalDate.parse("2025-04-10")
        )

        val customerCoupon1 = CustomerCoupon.issue(customer, coupon1).apply {
            status = CustomerCouponStatus.AVAILABLE
        }

        val customerCoupon2 = CustomerCoupon.issue(customer, coupon2).apply {
            status = CustomerCouponStatus.USED
        }

        every { customerCouponRepository.findAllByCustomerId(1L) } returns
            listOf(customerCoupon1, customerCoupon2)

        // 1. 고객 잔액 충전
        mockMvc.perform(
            patch("/balances/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "customerId": 1, "amount": 100000 }""")
        ).andExpect(status().isOk)

        // 2. 상품 목록 조회
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk)

        // 3. 상품 상세 조회
        mockMvc.perform(get("/products/1"))
            .andExpect(status().isOk)

        // 4. 인기 상품 조회
        mockMvc.perform(get("/products/popular?days=3"))
            .andExpect(status().isOk)

        // 5. 주문 생성
        val orderRequest = OrderRequest.Create(
            customerId = 1,
            items = listOf(
                OrderRequest.OrderItem(productId = 1, productOptionId = 2, quantity = 1),
                OrderRequest.OrderItem(productId = 2, productOptionId = 3, quantity = 1)
            )
        )

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest))
        ).andExpect(status().isCreated)

        // 6. 결제 생성
        val paymentRequest = PaymentRequest(
            orderId = 1L,
            couponId = null
        )

        mockMvc.perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest))
        ).andExpect(status().isCreated)

        // 7. 보유 쿠폰 목록
        mockMvc.perform(get("/coupons/customer/1"))
            .andExpect(status().isOk)

        // 8. 쿠폰 발급
        val couponRequest = CouponRequest.Issue(couponId = 1L, customerId = 1L)

        mockMvc.perform(
            post("/coupons/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(couponRequest))
        ).andExpect(status().isCreated)

        // 9. 잔액 조회
        mockMvc.perform(get("/balances/1"))
            .andExpect(status().isOk)

        // 10. 잔액 히스토리 조회
        mockMvc.perform(get("/balances/1/balance-histories"))
            .andExpect(status().isOk)
    }
}
