package kr.hhplus.be.server.support.customercontext

import kr.hhplus.be.server.support.exception.customer.CustomerContextMissingException

object CustomerContextHolder {
    private val threadLocal = ThreadLocal<CustomerContext?>()

    fun set(context: CustomerContext) = threadLocal.set(context)
    fun get(): CustomerContext = threadLocal.get() ?: throw CustomerContextMissingException()
    fun clear() = threadLocal.remove()
}
