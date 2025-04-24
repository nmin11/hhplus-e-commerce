package kr.hhplus.be.server.support.logging

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

@Component
class LoggingFilter : Filter {
    private val log = LoggerFactory.getLogger(LoggingFilter::class.java)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val wrappedRequest = ContentCachingRequestWrapper(request as HttpServletRequest)
        val wrappedResponse = ContentCachingResponseWrapper(response as HttpServletResponse)

        try {
            chain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            logRequest(wrappedRequest)
            logResponse(wrappedResponse)
            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun logRequest(request: ContentCachingRequestWrapper) {
        val body = String(request.contentAsByteArray, StandardCharsets.UTF_8)
        log.info(
            "[HTTP REQUEST] {} {}\nHeaders: {}\nBody: {}",
            request.method,
            request.requestURI,
            getHeaders(request),
            body.ifBlank { "<empty>" }
        )
    }

    private fun logResponse(response: ContentCachingResponseWrapper) {
        val body = String(response.contentAsByteArray, StandardCharsets.UTF_8)
        log.info(
            "[HTTP RESPONSE] {}\nHeaders: {}\nBody: {}",
            response.status,
            getHeaders(response),
            body.ifBlank { "<empty>" }
        )
    }

    private fun getHeaders(request: HttpServletRequest): Map<String, String> =
        request.headerNames.toList().associateWith { request.getHeader(it) }

    private fun getHeaders(response: HttpServletResponse): Map<String, String> =
        response.headerNames.associateWith { response.getHeader(it) }
}
