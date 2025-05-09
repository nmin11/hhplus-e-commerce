package kr.hhplus.be.server.support.aop

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LayeredCacheable(
    val resourceName: String,
    val displayName: String,
    val redisTtlSeconds: Long = 60 * 60 * 12
)
