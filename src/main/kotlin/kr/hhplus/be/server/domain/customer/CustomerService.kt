package kr.hhplus.be.server.domain.customer

import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository
) {
    fun validateCustomerExistence(customerId: Long) {
        if (!customerRepository.existsById(customerId)) {
            throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
    }

    fun getById(id: Long): Customer {
        return customerRepository.findById(id)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
    }
}
