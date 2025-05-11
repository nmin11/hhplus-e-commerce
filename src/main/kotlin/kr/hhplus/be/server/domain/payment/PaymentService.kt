package kr.hhplus.be.server.domain.payment

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {
    @Transactional
    fun create(payment: Payment): Payment {
        return paymentRepository.save(payment)
    }
}
