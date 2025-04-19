package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.domain.product.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        product1 = Product.create("hood", basePrice = 50_000)
        productRepository.save(product1)

        option1 = ProductOption.create(product1, optionName = "L", extraPrice = 5_000)
        productOptionRepository.save(option1)
        stockRepository.save(Stock.create(option1, quantity = 10))

        // 상품 2: 티셔츠
        product2 = Product.create("t-shirt", basePrice = 30_000)
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

    @Test
    @DisplayName("옵션이 상품에 속하지 않으면 주문 생성 실패")
    fun createOrder_shouldFail_whenOptionDoesNotBelongToProduct() {
        // given
        val anotherProduct = Product.create("chocolate", basePrice = 1000)
        productRepository.save(anotherProduct)

        val anotherOption = ProductOption.create(anotherProduct, optionName = "bundle", extraPrice = 5000)
        productOptionRepository.save(anotherOption)

        val command = OrderCommand.Create(
            customerId = customer.id,
            items = listOf(
                OrderCommand.OrderItem(
                    productId = product1.id, // 실제 옵션과 다른 상품
                    productOptionId = anotherOption.id,
                    quantity = 1
                )
            )
        )

        // when
        val exception = assertThrows<IllegalStateException> {
            orderFacade.createOrder(command)
        }

        // then
        assertThat(exception.message).isEqualTo("상품 옵션이 해당 상품에 속하지 않습니다.")
    }

    @Test
    @DisplayName("상품 재고가 부족하면 주문 생성 실패")
    fun createOrder_shouldFail_whenStockIsNotEnough() {
        // given
        val command = OrderCommand.Create(
            customerId = customer.id,
            items = listOf(
                OrderCommand.OrderItem(
                    productId = product1.id,
                    productOptionId = option1.id,
                    quantity = 999 // 재고를 초과하는 요청
                )
            )
        )

        // when
        val exception = assertThrows<IllegalStateException> {
            orderFacade.createOrder(command)
        }

        // then
        assertThat(exception.message).isEqualTo("재고가 10개 남아 있어서 주문이 불가능합니다.")
    }

    @Test
    @DisplayName("0개의 상품으로 주문 생성 시 예외 발생")
    fun createOrder_shouldFail_whenNoItemsProvided() {
        // given
        val command = OrderCommand.Create(
            customerId = customer.id,
            items = emptyList() // 비어 있는 주문 항목
        )

        // when
        val exception = assertThrows<IllegalArgumentException> {
            orderFacade.createOrder(command)
        }

        // then
        assertThat(exception.message).isEqualTo("주문 항목이 비어있습니다.")
    }
}
