package kr.hhplus.be.server.infrastructure.customer

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import org.springframework.stereotype.Repository

@Repository
class CustomerRepositoryImpl(
    private val customerJpaRepository: CustomerJpaRepository
) : CustomerRepository {
    override fun save(customer: Customer): Customer {
        return customerJpaRepository.save(customer)
    }

    override fun existsById(id: Long): Boolean {
        return customerJpaRepository.existsById(id)
    }

    override fun findById(id: Long): Customer? {
        return customerJpaRepository.findById(id).orElse(null)
    }
}
