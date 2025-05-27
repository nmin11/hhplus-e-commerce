package kr.hhplus.be.server.application.kafka

import kr.hhplus.be.server.domain.kafka.KafkaTestMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, KafkaTestMessage>
) {
    companion object {
        private const val TOPIC_NAME = "test-topic"
    }

    fun sendMessage(messageId: Long, name: String) {
        val message = KafkaTestMessage(messageId, name)
        kafkaTemplate.send(TOPIC_NAME, messageId.toString(), message)
    }
}
