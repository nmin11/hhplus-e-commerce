package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CustomerCouponAlreadyUsedException : BusinessException(
    code = "COUPON_ALREADY_USED",
    message = "이미 사용된 쿠폰입니다.",
    status = HttpStatus.CONFLICT
)
