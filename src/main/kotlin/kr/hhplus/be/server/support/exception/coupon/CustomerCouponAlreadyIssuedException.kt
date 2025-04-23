package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CustomerCouponAlreadyIssuedException : BusinessException(
    code = "COUPON_ALREADY_ISSUED",
    message = "해당 쿠폰은 이미 발급된 쿠폰입니다.",
    status = HttpStatus.CONFLICT
)
