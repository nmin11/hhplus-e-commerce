package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponService
import kr.hhplus.be.server.domain.coupon.event.CouponIssuedEvent
import kr.hhplus.be.server.domain.customer.CustomerService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponKafkaListener(
    private val couponService: CouponService,
    private val customerCouponService: CustomerCouponService,
    private val customerService: CustomerService
) {
    companion object {
        private const val TOPIC_NAME = "inside.coupon.issued"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    @KafkaListener(topics = [TOPIC_NAME])
    fun listen(events: List<CouponIssuedEvent>, ack: Acknowledgment) {
        log.info("[Kafka] [Coupon] 쿠폰 발급 메시지 수신: size=${events.size}")

        if (events.isEmpty()) return

        val groupedByCouponId = events.groupBy { it.couponId }
        val couponMap = groupedByCouponId.mapValues { (couponId, _) ->
            couponService.getById(couponId)
        }

        // 쿠폰 수량 차감
        groupedByCouponId.forEach { (couponId, groupEvents) ->
            val coupon = couponMap.getValue(couponId)
            couponService.decreaseQuantity(coupon, groupEvents.size)
        }

        // 고객에게 쿠폰 발급
        val customerCoupons = events.map {
            val coupon = couponMap.getValue(it.couponId)
            val customer = customerService.getById(it.customerId)
            CustomerCoupon.issue(customer, coupon)
        }
        customerCouponService.issueAll(customerCoupons)

        ack.acknowledge()
    }
}
