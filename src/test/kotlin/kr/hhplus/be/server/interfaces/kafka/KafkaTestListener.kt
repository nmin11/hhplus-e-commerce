package kr.hhplus.be.server.interfaces.kafka

import kr.hhplus.be.server.domain.kafka.KafkaTestMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList

@Component
class KafkaTestListener {
    companion object {
        val receivedProductIds: MutableList<Long> = CopyOnWriteArrayList()
    }

    private val log = LoggerFactory.getLogger(KafkaTestListener::class.java)

    @KafkaListener(topics = ["test-topic"], groupId = "test-consumer-group")
    fun listen(message: KafkaTestMessage, acknowledgment: Acknowledgment) {
        log.info("[Kafka] consumed: $message")
        receivedProductIds.add(message.messageId)
        acknowledgment.acknowledge()
    }

    fun clear() {
        receivedProductIds.clear()
    }
}
