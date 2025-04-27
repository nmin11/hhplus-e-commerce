package kr.hhplus.be.server.support.exception.product

import kr.hhplus.be.server.support.exception.BusinessException
import org.springframework.http.HttpStatus

class ProductOptionNameBlankException : BusinessException(
    code = "PRODUCT_OPTION_NAME_BLANK",
    message = "옵션 이름은 공백일 수 없습니다.",
    status = HttpStatus.BAD_REQUEST
)
