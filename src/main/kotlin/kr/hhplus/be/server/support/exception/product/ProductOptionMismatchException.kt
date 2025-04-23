package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class ProductOptionMismatchException : BusinessException(
    code = "PRODUCT_OPTION_MISMATCH",
    message = "상품 옵션이 해당 상품에 속하지 않습니다.",
    status = HttpStatus.CONFLICT
)
