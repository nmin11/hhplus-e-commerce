package kr.hhplus.be.server.support.exception.order

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class OrderNotFoundException : BusinessException(
    code = "ORDER_NOT_FOUND",
    message = "주문 정보를 찾을 수 없습니다.",
    status = HttpStatus.NOT_FOUND
)
