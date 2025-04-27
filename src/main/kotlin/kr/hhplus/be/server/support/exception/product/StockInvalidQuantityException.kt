package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class StockInvalidQuantityException : BusinessException(
    code = "STATISTIC_INVALID_QUANTITY",
    message = "재고 수량은 0 이상이어야 합니다.",
    status = HttpStatus.BAD_REQUEST
)
