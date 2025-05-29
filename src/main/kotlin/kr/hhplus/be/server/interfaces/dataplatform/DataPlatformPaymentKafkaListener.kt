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
        private const val TOPIC_NAME = "inside.payment.completed"
        private const val GROUP_ID = "data-platform-group"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [TOPIC_NAME], groupId = GROUP_ID)
    fun listen(event: PaymentCompletedEvent, ack: Acknowledgment) {
        log.info("[Kafka] [DataPlatform] 결제 완료 이벤트 수신")

        val orderPayload = DataPlatformCommand.OrderPayload.from(event)
        dataPlatformSender.send(orderPayload)

        ack.acknowledge()
    }
}
