package kr.hhplus.be.server.support.exception.balance

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class BalanceInsufficientException : BusinessException(
    code = "BALANCE_INSUFFICIENT",
    message = "잔액이 부족합니다.",
    status = HttpStatus.BAD_REQUEST
)
