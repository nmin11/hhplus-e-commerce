package kr.hhplus.be.server.domain.payment

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun deleteById(id: Long)
}
