package kr.hhplus.be.server.support.lock

import java.util.concurrent.TimeUnit

interface LockTemplate {
    fun lock(key: String, waitTime: Long, leaseTime: Long, timeUnit: TimeUnit): Boolean
    fun unlock(key: String)
}
