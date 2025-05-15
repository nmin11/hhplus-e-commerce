package kr.hhplus.be.server.domain.coupon

sealed class CouponInfo {
    enum class IssueResult(val code: Long) {
        SUCCESS(1L),
        NON_FOUND(-1L),
        ALREADY_ISSUED(-2L),
        INSUFFICIENT(-3L),
        UNKNOWN(0L);

        companion object {
            fun fromCode(code: Long?): IssueResult =
                entries.find { it.code == code } ?: UNKNOWN
        }
    }
}
