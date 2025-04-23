package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class StatisticInvalidSalesCountException : BusinessException(
    code = "STATISTIC_INVALID_SALES_COUNT",
    message = "판매 수량은 0보다 커야 합니다.",
    status = HttpStatus.BAD_REQUEST
)
