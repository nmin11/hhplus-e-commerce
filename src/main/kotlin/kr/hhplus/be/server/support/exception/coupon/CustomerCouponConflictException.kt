package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CustomerCouponConflictException : BusinessException(
    code = "CUSTOMER_COUPON_CONFLICT",
    message = "쿠폰 사용 요청이 중복되어 쿠폰을 사용할 수 없습니다.",
    status = HttpStatus.CONFLICT
)
