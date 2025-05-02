package kr.hhplus.be.server.support.exception.common

class MissingAnnotationException(targetName: String) : RuntimeException(
    "$targetName 을 위한 Annotation이 존재하지 않습니다."
)
