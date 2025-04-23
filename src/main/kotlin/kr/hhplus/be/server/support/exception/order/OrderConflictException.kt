package kr.hhplus.be.server.support.exception.order

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class OrderConflictException : BusinessException(
    code = "ORDER_CONFLICT",
    message = "주문에 대한 결제 요청이 중복되어 결제를 진행할 수 없습니다.",
    status = HttpStatus.CONFLICT
)
