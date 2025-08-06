package kr.hhplus.be.server.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

object RedisContainer : GenericContainer<Nothing>(
    DockerImageName.parse("redis:latest")
) {
    init {
        withExposedPorts(6379)
        withCommand("redis-server --requirepass root")
        start()
    }
}
