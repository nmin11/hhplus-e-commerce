package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class StockNotFoundException : BusinessException(
    code = "STOCK_NOT_FOUND",
    message = "재고 정보가 존재하지 않습니다.",
    status = HttpStatus.NOT_FOUND
)
