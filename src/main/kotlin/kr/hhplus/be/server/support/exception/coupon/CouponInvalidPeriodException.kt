package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CouponInvalidPeriodException : BusinessException(
    code = "COUPON_INVALID_PERIOD",
    message = "유효하지 않은 쿠폰입니다.",
    status = HttpStatus.BAD_REQUEST
)
