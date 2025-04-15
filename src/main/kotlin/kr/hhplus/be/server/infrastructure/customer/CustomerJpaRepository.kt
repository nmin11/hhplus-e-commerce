package kr.hhplus.be.server.infrastructure.customer

import kr.hhplus.be.server.domain.customer.Customer
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerJpaRepository : JpaRepository<Customer, Long> {
    fun save(customer: Customer): Customer
}
