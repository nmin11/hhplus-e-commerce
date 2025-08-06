package kr.hhplus.be.server

import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@Configuration
class TestContainersConfiguration {
    companion object {
        private val mysqlContainer: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("hhplus")
            .withUsername("root")
            .withPassword("root")
            .apply { start() }

        private val redisContainer = GenericContainer(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .withCommand("redis-server --requirepass root")
            .apply { start() }

        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"))
            .apply { start() }

        init {
            System.setProperty("spring.datasource.url", mysqlContainer.jdbcUrl + "?characterEncoding=UTF-8&serverTimezone=UTC")
            System.setProperty("spring.datasource.username", mysqlContainer.username)
            System.setProperty("spring.datasource.password", mysqlContainer.password)
            System.setProperty("jakarta.persistence.jdbc.url", mysqlContainer.jdbcUrl)
            System.setProperty("spring.redis.host", redisContainer.host)
            System.setProperty("spring.redis.port", redisContainer.firstMappedPort.toString())
            System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.bootstrapServers)
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            // MySQL
            registry.add("spring.datasource.url") { mysqlContainer.jdbcUrl + "?characterEncoding=UTF-8&serverTimezone=UTC" }
            registry.add("spring.datasource.username") { mysqlContainer.username }
            registry.add("spring.datasource.password") { mysqlContainer.password }

            // Redis
            registry.add("spring.redis.host") { redisContainer.host }
            registry.add("spring.redis.port") { redisContainer.getMappedPort(6379).toString() }

            // Kafka
            registry.add("spring.kafka.bootstrap-servers") { kafkaContainer.bootstrapServers }
        }
    }

    @PreDestroy
    fun preDestroy() {
        if (mysqlContainer.isRunning) mysqlContainer.stop()
        if (redisContainer.isRunning) redisContainer.stop()
        if (kafkaContainer.isRunning) kafkaContainer.stop()
    }
}
