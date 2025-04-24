package kr.hhplus.be.server.support.customercontext

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class CustomerContextInterceptorTest {
    private val resolver = mockk<CustomerContextResolver>()
    private val interceptor = CustomerContextInterceptor(resolver)

    @Test
    @DisplayName("X-CUSTOMER-ID 헤더가 존재하면 CustomerContext를 ThreadLocal에 저장")
    fun interceptorShouldStoreCustomerContextWhenHeaderExists() {
        // given
        val request = MockHttpServletRequest().apply {
            addHeader("X-CUSTOMER-ID", "11")
        }
        val response = MockHttpServletResponse()
        val handler = Any()

        val expectedContext = CustomerContext(
            customerId = 11L,
            username = "tester"
        )
        every { resolver.resolve(11L) } returns expectedContext

        // when
        val proceed = interceptor.preHandle(request, response, handler)

        // then
        assertThat(proceed).isTrue()
        assertThat(CustomerContextHolder.get()).isEqualTo(expectedContext)

        // cleanup
        interceptor.afterCompletion(request, response, handler, null)
        assertThatThrownBy { CustomerContextHolder.get() }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    @DisplayName("X-CUSTOMER-ID 헤더가 없으면 아무 동작 없이 그대로 통과")
    fun interceptorShouldPassThroughWhenHeaderMissing() {
        // given
        val request = MockHttpServletRequest() // 헤더 없음
        val response = MockHttpServletResponse()
        val handler = Any()

        // when
        val proceed = interceptor.preHandle(request, response, handler)

        // then
        assertThat(proceed).isTrue()
        assertThatThrownBy { CustomerContextHolder.get() }
            .isInstanceOf(IllegalStateException::class.java)
    }
}
