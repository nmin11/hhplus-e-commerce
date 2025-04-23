package kr.hhplus.be.server.support.exception.order

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class OrderNotPayableException(orderStatus: String) : BusinessException(
    code = "ORDER_NOT_PAYABLE",
    message = "결제 가능한 주문이 아닙니다. (현재 상태: $orderStatus)",
    status = HttpStatus.CONFLICT
)
