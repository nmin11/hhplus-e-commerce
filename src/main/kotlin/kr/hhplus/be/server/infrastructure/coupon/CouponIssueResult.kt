package kr.hhplus.be.server.infrastructure.coupon

enum class CouponIssueResult(val code: Long) {
    SUCCESS(1L),
    NON_FOUND(-1L),
    ALREADY_ISSUED(-2L),
    INSUFFICIENT(-3L),
    EXPIRED(-4L),
    UNKNOWN(0L);

    companion object {
        fun fromCode(code: Long?): CouponIssueResult =
            CouponIssueResult.entries.find { it.code == code } ?: UNKNOWN
    }
}
