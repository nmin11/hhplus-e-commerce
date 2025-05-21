package kr.hhplus.be.server.application.payment.event

enum class PaymentStep {
    STOCK_DECREASED,
    COUPON_APPLIED,
    BALANCE_DEDUCTED,
    PAYMENT_CREATED,
    STATISTIC_RECORDED
}
