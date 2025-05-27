package kr.hhplus.be.server.interfaces.kafka

import kr.hhplus.be.server.application.kafka.KafkaProducerService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Profile("test")
@RestController
@RequestMapping("/test/kafka")
class KafkaTestController(
    private val kafkaProducerService: KafkaProducerService
) {
    @PostMapping("/produce")
    fun produce(@RequestParam messageId: Long): ResponseEntity<String> {
        kafkaProducerService.sendMessage(messageId, "test message")
        return ResponseEntity.ok().body("Message sent for productId: $messageId")
    }
}
