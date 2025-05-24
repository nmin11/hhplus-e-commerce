package kr.hhplus.be.server.infrastructure.dataplatform

import kr.hhplus.be.server.application.dataplatform.DataPlatformCommand
import kr.hhplus.be.server.application.dataplatform.DataPlatformSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class MockApiDataPlatformSender(
    private val restClient: RestClient,

    @Value("\${external.data-platform.url}")
    private val apiUrl: String
) : DataPlatformSender {
    private val log = LoggerFactory.getLogger(MockApiDataPlatformSender::class.java)

    override fun send(command: DataPlatformCommand.OrderPayload) {
        try {
            val response = restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(command)
                .retrieve()
                .body(String::class.java)

            log.info("✅ 데이터 플랫폼 전송 성공: $response")
        } catch (e: Exception) {
            log.error("❌ 데이터 플랫폼 전송 실패: ${e.message}", e)
        }
    }
}
