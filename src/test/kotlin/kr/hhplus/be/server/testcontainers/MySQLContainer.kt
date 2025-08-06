package kr.hhplus.be.server.testcontainers

import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

object MySQLContainer : MySQLContainer<Nothing>(
    DockerImageName.parse("mysql:8.0")
) {
    init {
        withDatabaseName("hhplus")
        withUsername("root")
        withPassword("root")
        start()
    }
}
