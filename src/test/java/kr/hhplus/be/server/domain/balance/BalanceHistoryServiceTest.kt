package kr.hhplus.be.server.domain.balance

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BalanceHistoryServiceTest {
    private val balanceHistoryRepository = mockk<BalanceHistoryRepository>()
    private val balanceHistoryService = BalanceHistoryService(balanceHistoryRepository)

    @Test
    @DisplayName("잔액 변경 내역을 저장하고 반환")
    fun create_shouldSaveAndReturnBalanceHistory() {
        // given
        val history = BalanceHistory(
            customerId = 1L,
            changeType = BalanceChangeType.CHARGE,
            changeAmount = 50_000,
            totalAmount = 150_000
        )
        every { balanceHistoryRepository.save(history) } returns history

        // when
        val result = balanceHistoryService.create(history)

        // then
        assertThat(result).isEqualTo(history)
        verify(exactly = 1) { balanceHistoryRepository.save(history) }
    }

    @Test
    @DisplayName("고객 ID에 해당하는 모든 잔액 변경 내역을 조회")
    fun getAllByCustomerId_shouldReturnAllHistories() {
        // given
        val customerId = 1L
        val histories = listOf(
            BalanceHistory(customerId, BalanceChangeType.CHARGE, 50_000, 150_000),
            BalanceHistory(customerId, BalanceChangeType.USE, 75_000, 75_000)
        )
        every { balanceHistoryRepository.findAllByCustomerId(customerId) } returns histories

        // when
        val result = balanceHistoryService.getAllByCustomerId(customerId)

        // then
        assertThat(result).containsExactlyElementsOf(histories)
        verify(exactly = 1) { balanceHistoryRepository.findAllByCustomerId(customerId) }
    }
}
