package kr.hhplus.be.server.support.exception.payment

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class PaymentDiscountExceedsTotalPriceException : BusinessException(
    code = "PAYMENT_DISCOUNT_EXCEEDS_TOTAL_PRICE",
    message = "할인 금액은 총 주문 금액보다 낮아야 합니다.",
    status = HttpStatus.BAD_REQUEST
)
