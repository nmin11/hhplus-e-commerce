package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class StockInsufficientException : BusinessException(
    code = "STOCK_INSUFFICIENT",
    message = "재고가 부족합니다.",
    status = HttpStatus.CONFLICT
)
