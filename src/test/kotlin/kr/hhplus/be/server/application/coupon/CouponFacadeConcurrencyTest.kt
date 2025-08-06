package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import kr.hhplus.be.server.support.exception.coupon.CouponInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyIssuedException
import kr.hhplus.be.server.testcontainers.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration
import java.time.LocalDate
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CouponFacadeConcurrencyTest @Autowired constructor(
    private val couponFacade: CouponFacade,
    private val customerRepository: CustomerRepository,
    private val couponRepository: CouponRepository,
    private val customerCouponRepository: CustomerCouponRepository,
    private val stringRedisTemplate: StringRedisTemplate
) : AbstractIntegrationTest() {
    private lateinit var coupon: Coupon
    private lateinit var stockKey: String
    private lateinit var issuedKey: String

    @BeforeEach
    fun setup() {
        coupon = couponRepository.save(
            Coupon.createFixedDiscount(
                name = "concurrent-coupon",
                amount = 1_000,
                quantity = 3,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            )
        )

        stockKey = "coupon:stock:${coupon.id}"
        issuedKey = "coupon:issued:${coupon.id}"
        stringRedisTemplate.opsForValue().set(
            stockKey,
            coupon.totalQuantity.toString(),
            Duration.ofMinutes(1)
        )
        stringRedisTemplate.delete(issuedKey)
    }

    @Test
    @DisplayName("동일한 사용자가 동일한 쿠폰을 여러 번 발급 받는 경우 예외 발생")
    fun duplicateCouponIssueWithSameUser_shouldCauseRaceCondition() {
        // given
        val customer = customerRepository.save(Customer.create("concurrent-user"))
        val command = CouponCommand.Issue(customerId = customer.id, couponId = coupon.id)

        val numberOfThreads = 5
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val exceptions = Collections.synchronizedList(mutableListOf<Exception>())

        // when
        repeat(numberOfThreads) {
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
        assertThat(exceptions.count { it is CustomerCouponAlreadyIssuedException })
            .isEqualTo(numberOfThreads - 1)

        await()
            .pollInterval(Duration.ofMillis(500))
            .atMost(30, TimeUnit.SECONDS)
            .untilAsserted {
                val issuedCoupons = customerCouponRepository.findAllByCustomerId(customer.id)
                println("발급된 쿠폰 개수: ${issuedCoupons.size}")
                assertThat(issuedCoupons.size).isEqualTo(1)
            }
    }

    @Test
    @DisplayName("여러 명의 사용자가 동일한 쿠폰을 동시 발급 받는 경우 예외 발생")
    fun multipleUsersIssueSameCoupon_shouldCauseRaceCondition() {
        // given
        val customers = (1..5).map {
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
        assertThat(exceptions.count { it is CouponInsufficientException })
            .isEqualTo(numberOfThreads - coupon.totalQuantity)

        await()
            .pollInterval(Duration.ofMillis(500))
            .atMost(30, TimeUnit.SECONDS)
            .untilAsserted {
                val issuedCoupons = customerCouponRepository.findAllByCouponIn(listOf(coupon))
                println("발급된 쿠폰 개수: ${issuedCoupons.size}")
                assertThat(issuedCoupons.size).isEqualTo(coupon.totalQuantity)
            }
    }
}
