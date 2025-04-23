package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CustomerCouponNotFoundException : BusinessException(
    code = "CUSTOMER_COUPON_NOT_FOUND",
    message = "해당 쿠폰은 고객에게 발급되지 않았습니다.",
    status = HttpStatus.NOT_FOUND
)
