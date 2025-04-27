package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CouponInsufficientException : BusinessException(
    code = "COUPON_INSUFFICIENT",
    message = "쿠폰 수량이 모두 소진되었습니다.",
    status = HttpStatus.CONFLICT
)
