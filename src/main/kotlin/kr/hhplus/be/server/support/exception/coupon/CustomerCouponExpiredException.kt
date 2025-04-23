package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CustomerCouponExpiredException : BusinessException(
    code = "COUPON_EXPIRED",
    message = "사용 기간이 만료된 쿠폰입니다.",
    status = HttpStatus.CONFLICT
)
