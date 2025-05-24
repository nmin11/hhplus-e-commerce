package kr.hhplus.be.server.support.exception.payment

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class PaymentResultNotReadyException : BusinessException(
    code = "PAYMENT_RESULT_NOT_READY",
    message = "결제 결과가 아직 준비되지 않았습니다.",
    status = HttpStatus.INTERNAL_SERVER_ERROR
)
