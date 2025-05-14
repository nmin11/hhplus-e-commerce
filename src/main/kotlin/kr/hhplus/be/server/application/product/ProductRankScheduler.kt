package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.ProductRankService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductRankScheduler(
    private val productRankService: ProductRankService
) {
    @Scheduled(cron = "0 0 0 * * *")
    fun generateProductRank() {
        productRankService.refreshRank(LocalDate.now())
    }
}
