package kr.hhplus.be.server.support.customercontext

object CustomerContextHolder {
    private val threadLocal = ThreadLocal<CustomerContext?>()

    fun set(context: CustomerContext) = threadLocal.set(context)
    fun get(): CustomerContext = threadLocal.get() ?: throw IllegalStateException("고객 정보가 설정되지 않았습니다.")
    fun clear() = threadLocal.remove()
}
