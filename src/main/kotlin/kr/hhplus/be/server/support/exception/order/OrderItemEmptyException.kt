package kr.hhplus.be.server.support.exception.order

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class OrderItemEmptyException : BusinessException(
    code = "ORDER_ITEM_EMPTY",
    message = "주문 항목이 비어있습니다.",
    status = HttpStatus.BAD_REQUEST
)
