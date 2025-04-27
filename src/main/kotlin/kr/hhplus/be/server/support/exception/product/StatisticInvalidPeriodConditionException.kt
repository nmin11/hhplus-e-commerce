package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class StatisticInvalidPeriodConditionException : BusinessException(
    code = "STATISTIC_INVALID_PERIOD_CONDITION",
    message = "days, weeks, months 중 하나만 지정해야 합니다.",
    status = HttpStatus.BAD_REQUEST
)
