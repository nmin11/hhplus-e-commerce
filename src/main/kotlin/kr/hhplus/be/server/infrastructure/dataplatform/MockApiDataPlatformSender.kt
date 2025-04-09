package kr.hhplus.be.server.infrastructure.dataplatform

import kr.hhplus.be.server.application.dataplatform.DataPlatformCriteria
import kr.hhplus.be.server.application.dataplatform.DataPlatformSender
import kr.hhplus.be.server.domain.order.Order
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class MockApiDataPlatformSender(
    private val restClient: RestClient
) : DataPlatformSender {

    private val log = LoggerFactory.getLogger(MockApiDataPlatformSender::class.java)
    private val apiUrl = "https://67f65cb942d6c71cca61b523.mockapi.io/order"

    override fun send(order: Order) {
        val message = DataPlatformCriteria.OrderMessage.from(order)

        try {
            val response = restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(message)
                .retrieve()
                .body(String::class.java)

            log.info("✅ 데이터 플랫폼 전송 성공: $response")
        } catch (e: Exception) {
            log.error("❌ 데이터 플랫폼 전송 실패: ${e.message}", e)
        }
    }
}
