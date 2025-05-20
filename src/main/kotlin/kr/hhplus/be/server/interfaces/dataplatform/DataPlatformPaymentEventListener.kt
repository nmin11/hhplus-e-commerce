package kr.hhplus.be.server.interfaces.dataplatform

import kr.hhplus.be.server.application.dataplatform.DataPlatformCommand
import kr.hhplus.be.server.application.dataplatform.DataPlatformSender
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class DataPlatformPaymentEventListener(
    private val dataPlatformSender: DataPlatformSender
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PaymentCompletedEvent) {
        val orderPayload = DataPlatformCommand.OrderPayload.from(event)
        dataPlatformSender.send(orderPayload)
    }
}
