package kr.hhplus.be.server.testcontainers

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

object KafkaContainer : KafkaContainer(
    DockerImageName.parse("confluentinc/cp-kafka:7.5.1")
) {
    init {
        start()
    }
}
