package kr.hhplus.be.server.domain.balance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BalanceHistoryTest {
    @Test
    @DisplayName("충전 이력 생성 시 타입은 CHARGE")
    fun shouldCreateChargeHistory() {
        val history = BalanceHistory.charge(1L, 1000, 5000)

        assertThat(history.changeType).isEqualTo(BalanceChangeType.CHARGE)
        assertThat(history.changeAmount).isEqualTo(1000)
        assertThat(history.totalAmount).isEqualTo(5000)
    }

    @Test
    @DisplayName("사용 이력 생성 시 타입은 USE")
    fun shouldCreateUseHistory() {
        val history = BalanceHistory.use(1L, 3000, 2000)

        assertThat(history.changeType).isEqualTo(BalanceChangeType.USE)
        assertThat(history.changeAmount).isEqualTo(3000)
        assertThat(history.totalAmount).isEqualTo(2000)
    }
}
