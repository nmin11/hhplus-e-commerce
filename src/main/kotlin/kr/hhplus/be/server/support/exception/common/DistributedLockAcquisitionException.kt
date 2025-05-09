package kr.hhplus.be.server.support.exception.common

class DistributedLockAcquisitionException(lockKey: String) : RuntimeException(
    "Lock 획득에 실패했습니다. Lock Key: $lockKey"
)
