package kr.hhplus.be.server.support.customercontext

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class CustomerContextInterceptor(
    private val resolver: CustomerContextResolver
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val customerIdHeader = request.getHeader("X-CUSTOMER-ID") ?: return true

        val context = resolver.resolve(customerIdHeader.toLong())
        CustomerContextHolder.set(context)
        return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        CustomerContextHolder.clear()
    }
}
