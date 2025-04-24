package kr.hhplus.be.server.support.exception.balance

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class BalanceDeductFailedException : BusinessException(
    code = "BALANCE_DEDUCT_FAILED",
    message = "사용자 잔액 차감 요청이 중복되어 결제를 진행할 수 없습니다.",
    status = HttpStatus.CONFLICT
)
