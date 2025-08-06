package kr.hhplus.be.server.testcontainers

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractIntegrationTest {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                MySQLContainer.jdbcUrl + "?characterEncoding=UTF-8&serverTimezone=UTC"
            }
            registry.add("spring.datasource.username") { MySQLContainer.username }
            registry.add("spring.datasource.password") { MySQLContainer.password }
            registry.add("spring.redis.host") { RedisContainer.host }
            registry.add("spring.redis.port") { RedisContainer.getMappedPort(6379).toString() }
            registry.add("spring.kafka.bootstrap-servers") { KafkaContainer.bootstrapServers }
        }
    }
}
