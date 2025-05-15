package kr.hhplus.be.server.support.exception.coupon

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CouponIssueFailedException : BusinessException(
    code = "COUPON_ISSUE_FAILED",
    message = "쿠폰 발급에 실패했습니다.",
    status = HttpStatus.INTERNAL_SERVER_ERROR
)
