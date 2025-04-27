package kr.hhplus.be.server.support.exception.customer

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class CustomerInvalidNameException : BusinessException(
    code = "CUSTOMER_INVALID_NAME",
    message = "사용자 이름은 비어있을 수 없습니다.",
    status = HttpStatus.BAD_REQUEST
)
