package kr.hhplus.be.server.support.customercontext

import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.support.exception.customer.CustomerNotFoundException
import org.springframework.stereotype.Component

@Component
class CustomerContextResolver(
    private val customerRepository: CustomerRepository
) {
    fun resolve(customerId: Long): CustomerContext {
        val customer = customerRepository.findById(customerId)
            ?: throw CustomerNotFoundException()

        return CustomerContext(
            customerId = customer.id,
            username = customer.username,
        )
    }
}
