package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class ProductNameBlankException : BusinessException(
    code = "PRODUCT_NAME_BLANK",
    message = "상품 이름은 공백일 수 없습니다.",
    status = HttpStatus.BAD_REQUEST
)
