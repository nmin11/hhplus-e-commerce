package kr.hhplus.be.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.spyk
import kr.hhplus.be.server.domain.balance.*
import kr.hhplus.be.server.domain.customer.CustomerRepository
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

    @Test
    @DisplayName("전체 API 성공 흐름 테스트")
    fun allApiSuccessFlow() {
        every { customerRepository.existsById(1L) } returns true

        every { balanceRepository.findByCustomerId(1L) } returns Balance(1L, 150000)
        every { balanceHistoryRepository.findAllByCustomerId(1L) } returns
            listOf(
                BalanceHistory(
                    1L,
                    BalanceChangeType.CHARGE,
                    1000,
                    6000
                )
            )

        every { balanceRepository.save(any()) } returns Balance(1L, 100000)
        every { balanceHistoryRepository.save(any()) } returns
            BalanceHistory(
                1L,
                BalanceChangeType.CHARGE,
                100000,
                150000
            )

        val product1 = spyk(Product(name = "청바지", basePrice = 39000))
        every { product1.id } returns 1L
        val product2 = spyk(Product(name = "후드티", basePrice = 29000))
        every { product2.id } returns 2L
        val product3 = spyk(Product(name = "운동화", basePrice = 59000))
        every { product3.id } returns 3L
        val product4 = spyk(Product(name = "잠바", basePrice = 79000))
        every { product4.id } returns 4L
        val product5 = spyk(Product(name = "실내화", basePrice = 15000))
        every { product5.id } returns 5L
        every { productRepository.findAll() } returns listOf(product1, product2, product3)
        every { productRepository.findById(1L) } returns product1
        every { productOptionRepository.findAllByProductId(1L) } returns
            listOf(
                ProductOption(productId = 1L, optionName = "S", extraPrice = 0),
                ProductOption(productId = 2L, optionName = "M", extraPrice = 1000),
                ProductOption(productId = 3L, optionName = "L", extraPrice = 2000)
            )

        every { productRepository.findAllByIds(listOf(1L, 2L, 3L, 4L, 5L)) } returns
            listOf(product1, product2, product3, product4, product5)
        val stat1 = spyk(Statistic(productId = 1L, salesCount = 12))
        every { stat1.soldAt } returns LocalDateTime.now().minusDays(1)
        val stat2 = spyk(Statistic(productId = 2L, salesCount = 9))
        every { stat2.soldAt } returns LocalDateTime.now().minusDays(2)
        val stat3 = spyk(Statistic(productId = 3L, salesCount = 7))
        every { stat3.soldAt } returns LocalDateTime.now().minusDays(3)
        val stat4 = spyk(Statistic(productId = 4L, salesCount = 5))
        every { stat4.soldAt } returns LocalDateTime.now().minusDays(1)
        val stat5 = spyk(Statistic(productId = 5L, salesCount = 3))
        every { stat5.soldAt } returns LocalDateTime.now().minusDays(2)
        every { statisticRepository.findTop5BySoldAtAfterOrderBySalesCountDesc(any()) } returns
            listOf(stat1, stat2, stat3, stat4, stat5)

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
