package kr.hhplus.be.server.support.exception

import kr.hhplus.be.server.support.exception.common.DistributedLockAcquisitionException
import kr.hhplus.be.server.support.exception.customer.CustomerContextMissingException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse(
                code = "INVALID_ARGUMENT",
                message = ex.message ?: "잘못된 요청입니다.",
            ))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                code = "ILLEGAL_STATE",
                message = ex.message ?: "요청 상태가 올바르지 않습니다.",
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .internalServerError()
            .body(ErrorResponse(
                code = "INTERNAL_SERVER_ERROR",
                message = ex.message ?: "서버 내부 오류입니다.",
            ))
    }

    @ExceptionHandler(CustomerContextMissingException::class)
    fun handleMissingCustomerContext(ex: CustomerContextMissingException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("MISSING_CUSTOMER_CONTEXT", ex.message ?: "고객 정보가 없습니다."))
    }

    @ExceptionHandler(DistributedLockAcquisitionException::class)
    fun handleLockFail(ex: DistributedLockAcquisitionException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                code = "LOCK_CONFLICT",
                message = ex.message ?: "Lock 획득에 실패했습니다.",
            ))
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(ex: BusinessException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(ex.status)
            .body(ErrorResponse(
                code = ex.code,
                message = ex.message
            ))
    }
}
