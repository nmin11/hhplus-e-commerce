package kr.hhplus.be.server.support.exception.balance

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class BalanceNotFoundException : BusinessException(
    code = "BALANCE_NOT_FOUND",
    message = "잔액 정보가 존재하지 않습니다.",
    status = HttpStatus.NOT_FOUND
)
