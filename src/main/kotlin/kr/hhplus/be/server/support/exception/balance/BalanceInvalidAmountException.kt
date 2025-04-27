package kr.hhplus.be.server.support.exception.balance

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class BalanceInvalidAmountException : BusinessException(
    code = "BALANCE_INVALID_AMOUNT",
    message = "요청 금액은 0보다 커야 합니다.",
    status = HttpStatus.BAD_REQUEST
)
