package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.product.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderFacadeIntegrationTest @Autowired constructor(
    private val orderFacade: OrderFacade,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val stockRepository: StockRepository,
) {
    private lateinit var customer: Customer
    private lateinit var product1: Product
    private lateinit var product2: Product
    private lateinit var option1: ProductOption
    private lateinit var option2: ProductOption

    @BeforeEach
    fun setup() {
        customer = Customer.create("order-user")
        customerRepository.save(customer)

        // 상품 1: 후드
        product1 = Product.create("후드", basePrice = 50_000)
        productRepository.save(product1)

        option1 = ProductOption.create(product1, optionName = "L", extraPrice = 5_000)
        productOptionRepository.save(option1)
        stockRepository.save(Stock.create(option1, quantity = 10))

        // 상품 2: 티셔츠
        product2 = Product.create("티셔츠", basePrice = 30_000)
        productRepository.save(product2)

        option2 = ProductOption.create(product2, optionName = "M", extraPrice = 3_000)
        productOptionRepository.save(option2)
        stockRepository.save(Stock.create(option2, quantity = 10))
    }

    @Test
    @DisplayName("여러 개의 상품 옵션으로 주문을 생성")
    fun createOrder_withMultipleItems_shouldSucceed() {
        // given
        val command = OrderCommand.Create(
            customerId = customer.id,
            items = listOf(
                OrderCommand.OrderItem(
                    productId = product1.id,
                    productOptionId = option1.id,
                    quantity = 2
                ),
                OrderCommand.OrderItem(
                    productId = product2.id,
                    productOptionId = option2.id,
                    quantity = 3
                )
            )
        )

        // when
        val result = orderFacade.createOrder(command)

        // then
        assertThat(result.customerId).isEqualTo(customer.id)
        assertThat(result.items).hasSize(2)

        val expectedTotal =
            (50_000 + 5_000) * 2 + (30_000 + 3_000) * 3
        assertThat(result.totalPrice).isEqualTo(expectedTotal)
    }
}
