package kr.hhplus.be.server.application.dataplatform

import kr.hhplus.be.server.domain.order.Order

interface DataPlatformSender {
    fun send(order: Order)
}
