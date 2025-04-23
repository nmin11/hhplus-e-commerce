package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CouponNotFoundException : BusinessException(
    code = "COUPON_NOT_FOUND",
    message = "쿠폰 정보가 존재하지 않습니다.",
    status = HttpStatus.NOT_FOUND
)
