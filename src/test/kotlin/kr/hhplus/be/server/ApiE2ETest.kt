package kr.hhplus.be.server

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.domain.balance.*
import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.domain.product.*
import kr.hhplus.be.server.interfaces.coupon.CouponRequest
import kr.hhplus.be.server.interfaces.order.OrderRequest
import kr.hhplus.be.server.interfaces.payment.PaymentRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiE2ETest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val customerRepository: CustomerRepository,
    val balanceRepository: BalanceRepository,
    val productRepository: ProductRepository,
    val productOptionRepository: ProductOptionRepository,
    val statisticRepository: StatisticRepository,
    val stockRepository: StockRepository,
    val orderRepository: OrderRepository,
    val couponRepository: CouponRepository,
    val customerCouponRepository: CustomerCouponRepository,
) {
    private lateinit var customer: Customer
    private lateinit var coupon1: Coupon
    private lateinit var coupon2: Coupon
    private lateinit var product1: Product
    private lateinit var product2: Product
    private lateinit var optionM: ProductOption
    private lateinit var optionL: ProductOption
    private lateinit var order: Order

    @BeforeEach
    fun setup() {
        // 고객
        customer = Customer.create("tester")
        customerRepository.save(customer)

        // 잔액
        val balance = Balance.create(customer, 150000)
        balanceRepository.save(balance)

        // 상품 + 옵션
        product1 = Product.create("청바지", 39000)
        product2 = Product.create("후드티", 29000)
        productRepository.save(product1)
        productRepository.save(product2)

        optionM = ProductOption.create(product1, "M", 1000)
        optionL = ProductOption.create(product2, "L", 2000)
        productOptionRepository.save(optionM)
        productOptionRepository.save(optionL)

        // 재고
        stockRepository.save(Stock.create(optionM, 10))
        stockRepository.save(Stock.create(optionL, 10))

        // 통계
        val product3 = Product.create("운동화", 59000)
        val product4 = Product.create("잠바", 79000)
        val product5 = Product.create("실내화", 15000)
        productRepository.save(product3)
        productRepository.save(product4)
        productRepository.save(product5)

        val statistic1 = Statistic.create(product1, 12)
        val statistic2 = Statistic.create(product2, 9)
        val statistic3 = Statistic.create(product3, 7)
        val statistic4 = Statistic.create(product4, 5)
        val statistic5 = Statistic.create(product5, 3)
        statisticRepository.save(statistic1)
        statisticRepository.save(statistic2)
        statisticRepository.save(statistic3)
        statisticRepository.save(statistic4)
        statisticRepository.save(statistic5)

        // 주문 (결제 대상)
        order = Order.create(customer).apply {
            totalPrice = 40000 + 31000
            orderItems.addAll(
                listOf(
                    OrderItem.create(this, optionM, 1),
                    OrderItem.create(this, optionL, 1)
                )
            )
        }
        orderRepository.save(order)

        // 쿠폰 + 보유 쿠폰
        coupon1 = Coupon.createFixedDiscount(
            name = "첫 구매 할인",
            amount = 3000,
            quantity = 100,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(5)
        )
        coupon2 = Coupon.createRateDiscount(
            name = "봄맞이 프로모션",
            rate = 10,
            quantity = 50,
            startedAt = LocalDate.now().minusDays(10),
            expiredAt = LocalDate.now().minusDays(1))
        couponRepository.save(coupon1)
        couponRepository.save(coupon2)
    }

    @Test
    @DisplayName("전체 API 성공 흐름 테스트")
    fun allApiSuccessFlow() {
        // 1. 고객 잔액 충전
        mockMvc.perform(
            patch("/balances/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "customerId": ${customer.id}, "amount": 100000 }""")
        ).andExpect(status().isOk)

        // 2. 상품 목록 조회
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk)

        // 3. 상품 상세 조회
        mockMvc.perform(get("/products/${product1.id}"))
            .andExpect(status().isOk)

        // 4. 인기 상품 조회
        mockMvc.perform(get("/products/popular?days=3"))
            .andExpect(status().isOk)

        // 5. 주문 생성
        val orderRequest = OrderRequest.Create(
            customerId = customer.id,
            items = listOf(
                OrderRequest.OrderItem(productId = product1.id, productOptionId = optionM.id, quantity = 1),
                OrderRequest.OrderItem(productId = product2.id, productOptionId = optionL.id, quantity = 1)
            )
        )
        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest))
        ).andExpect(status().isCreated)

        // 6. 결제 생성
        val paymentRequest = PaymentRequest(orderId = order.id, couponId = null)
        mockMvc.perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest))
        ).andExpect(status().isCreated)

        // 7. 보유 쿠폰 목록
        mockMvc.perform(get("/coupons/customer/${customer.id}"))
            .andExpect(status().isOk)

        // 8. 쿠폰 발급
        val couponRequest = CouponRequest.Issue(couponId = coupon1.id, customerId = customer.id)
        mockMvc.perform(
            post("/coupons/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(couponRequest))
        ).andExpect(status().isCreated)

        // 9. 잔액 조회
        mockMvc.perform(get("/balances/${customer.id}"))
            .andExpect(status().isOk)

        // 10. 잔액 히스토리 조회
        mockMvc.perform(get("/balances/${customer.id}/balance-histories"))
            .andExpect(status().isOk)
    }
}
