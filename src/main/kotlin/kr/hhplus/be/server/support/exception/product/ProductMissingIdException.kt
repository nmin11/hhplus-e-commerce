package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class ProductMissingIdException : BusinessException(
    code = "PRODUCT_MISSING_ID",
    message = "상품 ID 또는 상품 옵션 ID가 존재하지 않습니다.",
    status = HttpStatus.BAD_REQUEST
)
