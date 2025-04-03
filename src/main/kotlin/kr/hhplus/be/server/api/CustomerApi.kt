package kr.hhplus.be.server.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.dto.BalanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/customers")
interface CustomerApi {
    @Operation(summary = "고객 잔액 조회")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BalanceResponse::class),
                    examples = [ExampleObject(value = """{"customerId":1,"amount":10000}""")]
                )
            ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = [
                Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(value = """{"error":"사용자를 찾을 수 없습니다."}""")]
                )
            ]
        )
    )
    @GetMapping("/{id}/balance")
    fun getBalance(@PathVariable id: Long): ResponseEntity<BalanceResponse>
}
