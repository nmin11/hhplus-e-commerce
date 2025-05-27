package kr.hhplus.be.server.interfaces.dataplatform

import kr.hhplus.be.server.application.dataplatform.DataPlatformCommand
import kr.hhplus.be.server.application.dataplatform.DataPlatformSender
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class DataPlatformPaymentKafkaListener(
    private val dataPlatformSender: DataPlatformSender
) {
    companion object {
        private const val TOPIC_NAME = "payment-completed-topic"
        private const val GROUP_ID = "data-platform-consumer-group"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [TOPIC_NAME], groupId = GROUP_ID)
    fun listen(event: PaymentCompletedEvent, acknowledgment: Acknowledgment) {
        log.info("[Kafka] [DataPlatform] 결제 완료 이벤트 수신")

        val orderPayload = DataPlatformCommand.OrderPayload.from(event)
        dataPlatformSender.send(orderPayload)

        log.info("[Kafka] [DataPlatform] 데이터 플랫폼에 주문 정보 전송 완료")
        acknowledgment.acknowledge()
    }
}
