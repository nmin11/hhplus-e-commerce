package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.support.exception.coupon.CouponInsufficientException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
class CouponFacadeDistributedLockTest @Autowired constructor(
    private val couponFacade: CouponFacade,
    private val couponRepository: CouponRepository,
    private val customerRepository: CustomerRepository,
    private val customerCouponRepository: CustomerCouponRepository
) {
    private lateinit var coupon: Coupon

    @BeforeEach
    fun setup() {
        coupon = couponRepository.save(
            Coupon.createFixedDiscount(
                name = "distributed-coupon",
                amount = 1_000,
                quantity = 1,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            )
        )
    }

    @Test
    @DisplayName("쿠폰 동시 발급에 대한 분산 락 적용 테스트")
    fun issueCoupon_distributedLockTest() {
        // given
        val customers = (1..3).map {
            customerRepository.save(Customer.create("user$it"))
        }

        val numberOfThreads = customers.size
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        customers.forEach { customer ->
            val command = CouponCommand.Issue(customerId = customer.id, couponId = coupon.id)
            executor.submit {
                try {
                    couponFacade.issueCouponToCustomer(command)
                } catch (e: Exception) {
                    exceptions.add(e)
                    println("❗ 예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // then
        val issuedCoupons = customerCouponRepository.findAllByCouponIn(listOf(coupon))
        println("발급된 쿠폰 개수: ${issuedCoupons.size}")
        assertThat(issuedCoupons.size).isEqualTo(coupon.totalQuantity)
        assertThat(exceptions.count { it is CouponInsufficientException })
            .isEqualTo(numberOfThreads - coupon.totalQuantity)
    }
}
