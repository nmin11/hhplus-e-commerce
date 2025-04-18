package kr.hhplus.be.server.domain.customer

interface CustomerRepository {
    fun save(customer: Customer): Customer
    fun existsById(id: Long): Boolean
    fun findById(id: Long): Customer?
}
