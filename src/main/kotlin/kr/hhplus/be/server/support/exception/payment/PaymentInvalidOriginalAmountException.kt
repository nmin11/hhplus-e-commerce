package kr.hhplus.be.server.support.exception.payment

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class PaymentInvalidOriginalAmountException : BusinessException(
    code = "PAYMENT_INVALID_ORIGINAL_PRICE",
    message = "기존 금액은 0 이상이어야 합니다.",
    status = HttpStatus.BAD_REQUEST
)
