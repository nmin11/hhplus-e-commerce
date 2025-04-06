package kr.hhplus.be.server

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.interfaces.dto.request.CouponIssueRequest
import kr.hhplus.be.server.interfaces.dto.request.OrderItemRequest
import kr.hhplus.be.server.interfaces.dto.request.OrderRequest
import kr.hhplus.be.server.interfaces.dto.request.PaymentRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ApiE2ETest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("전체 API 성공 흐름 테스트")
    fun allApiSuccessFlow() {
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
        val orderRequest = OrderRequest(
            customerId = 1,
            items = listOf(
                OrderItemRequest(productId = 1, productOptionId = 2, quantity = 1),
                OrderItemRequest(productId = 2, productOptionId = 3, quantity = 1)
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
        val couponRequest = CouponIssueRequest(customerId = 1L)

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
