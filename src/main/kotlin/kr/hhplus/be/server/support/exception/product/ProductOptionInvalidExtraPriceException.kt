package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class ProductOptionInvalidExtraPriceException : BusinessException(
    code = "PRODUCT_OPTION_INVALID_EXTRA_PRICE",
    message = "추가 가격은 0 이상이어야 합니다.",
    status = HttpStatus.BAD_REQUEST
)
