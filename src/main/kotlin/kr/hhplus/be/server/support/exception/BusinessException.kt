package kr.hhplus.be.server.support.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val code: String,
    override val message: String,
    val status: HttpStatus
) : RuntimeException(message)
