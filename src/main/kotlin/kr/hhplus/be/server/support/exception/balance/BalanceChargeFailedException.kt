package kr.hhplus.be.server.support.exception.balance

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class BalanceChargeFailedException : BusinessException(
    code = "BALANCE_CHARGE_FAILED",
    message = "지금은 충전을 진행할 수 없습니다. 잠시 후 다시 시도해주세요.",
    status = HttpStatus.SERVICE_UNAVAILABLE
)
