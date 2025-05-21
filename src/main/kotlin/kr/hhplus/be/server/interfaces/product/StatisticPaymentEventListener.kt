package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.product.Statistic
import kr.hhplus.be.server.domain.product.event.StatisticEventPublisher
import kr.hhplus.be.server.domain.product.event.StatisticRecordRequestedEvent
import kr.hhplus.be.server.domain.product.event.StatisticRecordedEvent
import kr.hhplus.be.server.domain.product.StatisticService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StatisticPaymentEventListener(
    private val statisticService: StatisticService,
    private val statisticEventPublisher: StatisticEventPublisher
) {
    @Transactional
    @EventListener
    fun handle(event: StatisticRecordRequestedEvent) {
        event.items.forEach {
            val statistic = Statistic.create(
                product = it.product,
                salesCount = it.quantity
            )
            statisticService.record(statistic)
        }

        statisticEventPublisher.publish(StatisticRecordedEvent(event.orderId))
    }
}
