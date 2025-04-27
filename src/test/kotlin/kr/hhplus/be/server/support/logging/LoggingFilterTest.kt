package kr.hhplus.be.server.support.logging

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

class LoggingFilterTest {
    private val filter = LoggingFilter()

    @Test
    @DisplayName("필터가 HTTP 요청 및 응답을 로깅")
    fun loggingFilter_shouldLoggingAboutHttpRequestAndHttpResponse() {
        // given
        val mockRequest = MockHttpServletRequest("POST", "/api/test").apply {
            contentType = "application/json"
            setContent("""{"message":"hello"}""".toByteArray())
        }
        val mockResponse = MockHttpServletResponse()

        val capturedRequest = slot<ServletRequest>()
        val capturedResponse = slot<ServletResponse>()

        val mockChain = mockk<FilterChain>()
        every { mockChain.doFilter(capture(capturedRequest), capture(capturedResponse)) } just Runs

        // when
        filter.doFilter(mockRequest, mockResponse, mockChain)

        // then
        Assertions.assertThat(capturedRequest.captured).isInstanceOf(ContentCachingRequestWrapper::class.java)
        Assertions.assertThat(capturedResponse.captured).isInstanceOf(ContentCachingResponseWrapper::class.java)

        verify(exactly = 1) { mockChain.doFilter(any(), any()) }
    }
}
