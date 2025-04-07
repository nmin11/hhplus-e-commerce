package kr.hhplus.be.server.domain.customer

interface CustomerRepository {
    fun save(customer: Customer): Customer
}
