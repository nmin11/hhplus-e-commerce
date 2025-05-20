package kr.hhplus.be.server.application.dataplatform

interface DataPlatformSender {
    fun send(command: DataPlatformCommand.OrderPayload)
}
