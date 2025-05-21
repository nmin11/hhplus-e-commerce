package kr.hhplus.be.server.application.payment.event

object PaymentExecutionContextHolder {
    private val threadLocal = ThreadLocal<PaymentExecutionContext>()

    fun set(context: PaymentExecutionContext) = threadLocal.set(context)
    fun get(): PaymentExecutionContext = threadLocal.get()
    fun clear() = threadLocal.remove()
}
