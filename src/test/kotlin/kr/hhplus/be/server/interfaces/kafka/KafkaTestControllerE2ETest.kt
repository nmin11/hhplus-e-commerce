package kr.hhplus.be.server.interfaces.kafka

import kr.hhplus.be.server.testcontainers.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.util.concurrent.TimeUnit

@AutoConfigureMockMvc
class KafkaTestControllerE2ETest : AbstractIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var listener: KafkaTestListener

    @Test
    @DisplayName("Kafka 메시지 Produce & Consume 테스트")
    fun shouldProduceAndConsumeMessageSuccessfully() {
        // given
        listener.clear()
        val messageId = 101L

        // when
        mockMvc.perform(
            post("/test/kafka/produce")
                .param("messageId", messageId.toString())
        ).andExpect(status().isOk)

        // then
        await()
            .pollInterval(Duration.ofMillis(500))
            .atMost(30, TimeUnit.SECONDS)
            .untilAsserted {
                assertThat(KafkaTestListener.receivedProductIds)
                    .contains(messageId)
            }
    }
}
