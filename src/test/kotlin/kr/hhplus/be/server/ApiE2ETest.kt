package kr.hhplus.be.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.server.domain.balance.*
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
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
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
        val customer = Customer(username = "tester").apply { id = 1L }

        every { customerRepository.existsById(1L) } returns true

        every { customerRepository.findById(1L) } returns customer

        val balance = Balance(customer = customer, amount = 150000).apply { id = 1L }
        every { balanceRepository.findByCustomerId(1L) } returns balance

        val history = BalanceHistory(
            customer = customer,
            changeType = BalanceChangeType.CHARGE,
            changeAmount = 1000,
            totalAmount = 6000
        ).apply { id = 1L }

        every { balanceHistoryRepository.findAllByCustomerId(1L) } returns listOf(history)

        every { balanceRepository.save(any()) } answers { firstArg() }
        every { balanceHistoryRepository.save(any()) } answers { firstArg() }

        val product1 = Product(name = "청바지", basePrice = 39000).apply { id = 1L }
        val product2 = Product(name = "후드티", basePrice = 29000).apply { id = 2L }
        val product3 = Product(name = "운동화", basePrice = 59000).apply { id = 3L }
        val product4 = Product(name = "잠바", basePrice = 79000).apply { id = 4L }
        val product5 = Product(name = "실내화", basePrice = 15000).apply { id = 5L }

        every { productRepository.findAll() } returns listOf(product1, product2, product3)
        every { productRepository.findById(1L) } returns product1
        every { productRepository.findById(2L) } returns product2

        every { productOptionRepository.findAllByProductId(1L) } returns listOf(
            ProductOption(product = product1, optionName = "S", extraPrice = 0).apply { id = 1L },
            ProductOption(product = product1, optionName = "M", extraPrice = 1000).apply { id = 2L },
            ProductOption(product = product1, optionName = "L", extraPrice = 2000).apply { id = 3L }
        )

        every {
            productRepository.findAllByIds(listOf(1L, 2L, 3L, 4L, 5L))
        } returns listOf(product1, product2, product3, product4, product5)

        val stat1 = Statistic(product = product1, salesCount = 12).apply {
            id = 1L
            soldAt = LocalDateTime.now().minusDays(1)
        }
        val stat2 = Statistic(product = product2, salesCount = 9).apply {
            id = 2L
            soldAt = LocalDateTime.now().minusDays(2)
        }
        val stat3 = Statistic(product = product3, salesCount = 7).apply {
            id = 3L
            soldAt = LocalDateTime.now().minusDays(3)
        }
        val stat4 = Statistic(product = product4, salesCount = 5).apply {
            id = 4L
            soldAt = LocalDateTime.now().minusDays(1)
        }
        val stat5 = Statistic(product = product5, salesCount = 3).apply {
            id = 5L
            soldAt = LocalDateTime.now().minusDays(2)
        }

        every {
            statisticRepository.findTop5BySoldAtAfterOrderBySalesCountDesc(any())
        } returns listOf(stat1, stat2, stat3, stat4, stat5)

        val optionM = ProductOption(product = product1, optionName = "M", extraPrice = 1000).apply { id = 2L }
        val optionL = ProductOption(product = product2, optionName = "L", extraPrice = 2000).apply { id = 3L }

        every { productOptionRepository.findById(2L) } returns optionM
        every { productOptionRepository.findById(3L) } returns optionL

        val stockOptionM = Stock(productOption = optionM, quantity = 10).apply { id = 2L }
        val stockOptionL = Stock(productOption = optionL, quantity = 10).apply { id = 3L }

        every { stockRepository.findByProductOptionId(optionM.id!!) } returns stockOptionM
        every { stockRepository.findByProductOptionId(optionL.id!!) } returns stockOptionL

        val order = Order(
            customer = customer,
            totalPrice = 40000 + 31000
        ).apply {
            id = 1L
            orderItems.addAll(
                listOf(
                    OrderItem(order = this, productOption = optionM, quantity = 1, subtotalPrice = 40000),
                    OrderItem(order = this, productOption = optionL, quantity = 1, subtotalPrice = 31000)
                )
            )
        }

        every { orderRepository.save(any()) } returns order

        every { orderRepository.findById(1L) } returns order

        every { statisticRepository.save(any()) } answers { firstArg() }

        every { paymentRepository.save(any()) } answers {
            val payment = firstArg<Payment>()
            payment.id = 1L
            payment
        }

        // 1. 고객 잔액 충전
        mockMvc.perform(
            patch("/customers/1/balance/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "amount": 100000 }""")
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
        mockMvc.perform(get("/customers/1/coupons"))
            .andExpect(status().isOk)

        // 8. 쿠폰 발급
        val couponRequest = CouponRequest.Issue(customerId = 1L)

        mockMvc.perform(
            post("/coupons/1/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(couponRequest))
        ).andExpect(status().isCreated)

        // 9. 잔액 조회
        mockMvc.perform(get("/customers/1/balance"))
            .andExpect(status().isOk)

        // 10. 잔액 히스토리 조회
        mockMvc.perform(get("/customers/1/balance-histories"))
            .andExpect(status().isOk)
    }
}
