package kr.hhplus.be.server.domain.customer

import kr.hhplus.be.server.support.exception.customer.CustomerNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository
) {
    fun validateCustomerExistence(customerId: Long) {
        if (!customerRepository.existsById(customerId)) {
            throw CustomerNotFoundException()
        }
    }

    fun getById(id: Long): Customer {
        return customerRepository.findById(id)
            ?: throw CustomerNotFoundException()
    }
}
