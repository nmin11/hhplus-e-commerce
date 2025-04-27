package kr.hhplus.be.server.support.exception.customer

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CustomerNotFoundException : BusinessException(
    code = "CUSTOMER_NOT_FOUND",
    message = "사용자를 찾을 수 없습니다.",
    status = HttpStatus.NOT_FOUND
)
