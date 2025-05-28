package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.event.CouponEventPublisher
import kr.hhplus.be.server.domain.coupon.event.CouponIssuedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUsedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component("kafkaCouponEventPublisher")
class KafkaCouponEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : CouponEventPublisher {
    override fun publish(event: CouponUsedEvent) {
        kafkaTemplate.send("inside.coupon.used", event.orderId.toString(), event)
    }

    override fun publish(event: CouponIssuedEvent) {
        kafkaTemplate.send("inside.coupon.issued", event.couponId.toString(), event)
    }
}
