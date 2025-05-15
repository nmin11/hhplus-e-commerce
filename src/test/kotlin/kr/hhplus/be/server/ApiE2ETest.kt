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
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
    val stringRedisTemplate: StringRedisTemplate
) {
    private lateinit var customer: Customer
    private lateinit var coupon1: Coupon
    private lateinit var coupon2: Coupon
    private lateinit var product1: Product
    private lateinit var product2: Product
    private lateinit var optionM: ProductOption
    private lateinit var optionL: ProductOption
    private lateinit var order: Order
    private lateinit var issuedKey: String

    @BeforeEach
    fun setup() {
        // 고객
        customer = Customer.create("tester")
        customerRepository.save(customer)

        // 잔액
        val balance = Balance.create(customer, 150000)
        balanceRepository.save(balance)

        // 상품 + 옵션
        product1 = Product.create("coat", 39_000)
        product2 = Product.create("jean", 29_000)
        productRepository.save(product1)
        productRepository.save(product2)

        optionM = ProductOption.create(product1, "M", 1_000)
        optionL = ProductOption.create(product2, "L", 2_000)
        productOptionRepository.save(optionM)
        productOptionRepository.save(optionL)

        // 재고
        stockRepository.save(Stock.create(optionM, 10))
        stockRepository.save(Stock.create(optionL, 10))

        // 통계
        val product3 = Product.create("shoo", 59_000)
        val product4 = Product.create("sneakers", 79_000)
        val product5 = Product.create("slip", 15_000)
        productRepository.save(product3)
        productRepository.save(product4)
        productRepository.save(product5)

        val statistic1 = Statistic.create(product1, 120)
        val statistic2 = Statistic.create(product2, 90)
        val statistic3 = Statistic.create(product3, 70)
        val statistic4 = Statistic.create(product4, 50)
        val statistic5 = Statistic.create(product5, 30)
        statisticRepository.save(statistic1)
        statisticRepository.save(statistic2)
        statisticRepository.save(statistic3)
        statisticRepository.save(statistic4)
        statisticRepository.save(statistic5)

        // 주문 생성
        order = Order.create(customer).apply {
            totalPrice = (product1.basePrice + optionM.extraPrice) + (product2.basePrice + optionL.extraPrice)
            orderItems.addAll(
                listOf(
                    OrderItem.create(this, optionM, 1),
                    OrderItem.create(this, optionL, 1)
                )
            )
        }
        orderRepository.save(order)

        // 쿠폰 생성
        coupon1 = Coupon.createFixedDiscount(
            name = "첫 구매 할인",
            amount = 3_000,
            quantity = 100,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(5)
        )
        coupon2 = Coupon.createRateDiscount(
            name = "봄맞이 프로모션",
            rate = 10,
            quantity = 50,
            startedAt = LocalDate.now().minusDays(10),
            expiredAt = LocalDate.now().plusDays(20))
        couponRepository.save(coupon1)
        couponRepository.save(coupon2)

        stringRedisTemplate.opsForValue().set(
            "coupon:stock:${coupon1.id}",
            coupon1.totalQuantity.toString(),
            Duration.ofMinutes(1)
        )
        stringRedisTemplate.opsForValue().set(
            "coupon:stock:${coupon2.id}",
            coupon2.totalQuantity.toString(),
            Duration.ofMinutes(1)
        )

        val customerCoupon1 = CustomerCoupon.issue(customer, coupon1)
        customerCouponRepository.save(customerCoupon1)

        issuedKey = "coupon:issued:${coupon1.id}"
        stringRedisTemplate.opsForSet().add(
            issuedKey,
            customer.id.toString()
        )
        stringRedisTemplate.expire(issuedKey, Duration.ofMinutes(1))
    }

    @AfterEach
    fun cleanUp() {
        stringRedisTemplate.delete(issuedKey)
    }

    @Test
    @DisplayName("전체 API 성공 흐름 테스트")
    fun allApiSuccessFlow() {
        // 1. 고객 잔액 충전
        var totalAmount = 250_000
        mockMvc.perform(
            patch("/balances/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "customerId": ${customer.id}, "amount": 100000 }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.customerId").value(customer.id))
            .andExpect(jsonPath("$.amount").value(totalAmount))

        // 2. 상품 목록 조회
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].name", hasItems("coat", "jean", "shoo", "sneakers", "slip")))

        // 3. 상품 상세 조회
        mockMvc.perform(get("/products/${product1.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("coat"))
            .andExpect(jsonPath("$.basePrice").value(39_000))

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
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.customerId").value(customer.id))
            .andExpect(jsonPath("$.totalPrice").value(
                product1.basePrice + optionM.extraPrice + product2.basePrice + optionL.extraPrice
            ))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].productName").value(product1.name))
            .andExpect(jsonPath("$.items[0].optionName").value(optionM.optionName))
            .andExpect(jsonPath("$.items[0].quantity").value(1))
            .andExpect(jsonPath("$.items[0].subtotalPrice").value(product1.basePrice + optionM.extraPrice))
            .andExpect(jsonPath("$.items[1].productName").value(product2.name))
            .andExpect(jsonPath("$.items[1].optionName").value(optionL.optionName))
            .andExpect(jsonPath("$.items[1].quantity").value(1))
            .andExpect(jsonPath("$.items[1].subtotalPrice").value(product2.basePrice + optionL.extraPrice))

        // 6. 결제 생성
        val paymentRequest = PaymentRequest(orderId = order.id, couponId = coupon1.id)
        val paidPrice = order.totalPrice - coupon1.discountAmount
        totalAmount -= paidPrice
        mockMvc.perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.orderId").value(order.id))
            .andExpect(jsonPath("$.customerId").value(customer.id))
            .andExpect(jsonPath("$.originalPrice").value(order.totalPrice))
            .andExpect(jsonPath("$.discountAmount").value(coupon1.discountAmount))
            .andExpect(jsonPath("$.discountedPrice").value(paidPrice))

        // 7. 쿠폰 발급
        val couponRequest = CouponRequest.Issue(couponId = coupon2.id, customerId = customer.id)
        mockMvc.perform(
            post("/coupons/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(couponRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.couponId").value(coupon2.id))
            .andExpect(jsonPath("$.customerId").value(customer.id))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))

        // 8. 보유 쿠폰 목록
        mockMvc.perform(get("/coupons/customer/${customer.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("첫 구매 할인"))
            .andExpect(jsonPath("$[0].discountType").value("FIXED"))
            .andExpect(jsonPath("$[0].discountAmount").value(3_000))
            .andExpect(jsonPath("$[0].status").value("USED"))
            .andExpect(jsonPath("$[1].name").value("봄맞이 프로모션"))
            .andExpect(jsonPath("$[1].discountType").value("RATE"))
            .andExpect(jsonPath("$[1].discountAmount").value(10))
            .andExpect(jsonPath("$[1].status").value("AVAILABLE"))

        // 9. 잔액 조회
        mockMvc.perform(get("/balances/${customer.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.customerId").value(customer.id))
            .andExpect(jsonPath("$.amount").value(totalAmount))

        // 10. 잔액 히스토리 조회
        mockMvc.perform(get("/balances/${customer.id}/balance-histories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].changeType").value("CHARGE"))
            .andExpect(jsonPath("$[0].changeAmount").value(100_000))
            .andExpect(jsonPath("$[0].totalAmount").value(250_000))
            .andExpect(jsonPath("$[1].changeType").value("USE"))
            .andExpect(jsonPath("$[1].changeAmount").value(paidPrice))
            .andExpect(jsonPath("$[1].totalAmount").value(totalAmount))
    }
}
