# Redis 기반 쿠폰 발급 기능 설계 및 개발 회고

## 1. 설계 방향

우리 E-커머스 서비스는 앞으로 신규 사용자 유치를 위한 다양한 쿠폰 이벤트를 자주 진행하게 될 것이며, 이로 인해 짧은 시간 내 대량의 요청이 몰리는 **스파이크 트래픽**이 발생할 수 있습니다.  
특히 선착순 쿠폰 발급 기능은 수량이 한정되어 있는 특성상, 하나의 쿠폰에 수십에서 수백 명의 사용자가 동시에 요청을 보내는 상황이 자주 발생하게 됩니다.  


기존 방식처럼 DB에 직접 접근한 후 Lock을 통한 제어만으로는 이러한 트래픽을 효율적으로 감당하기 어렵고, **Lock 경합, 응답 지연, 서버 부하**와 같은 문제가 반복적으로 발생할 수 있습니다.  

이러한 문제를 해결하기 위해 저는 Redis의 **단일 스레드 기반 처리 특성**과 **Lua Script를 통한 원자성 보장** 기능을 활용한 구조로 설계를 전환했습니다.  
이 설계는 높은 요청량에도 일관성과 안정성을 유지할 수 있으며, Redis의 빠른 조회 성능을 바탕으로 조건이 맞지 않을 경우 즉시 실패시키는 **Fail-Fast 전략**을 적용할 수 있다는 점에서 매우 효과적인 방식이라 판단했습니다.

<br>

## 2. 설계 세부사항

### 쿠폰 수량 관리

- Redis 자료구조: Strings
- Key 패턴: `coupon:stock:{couponId}`
- 쿠폰의 남은 수량을 문자열 숫자 형식으로 저장
- TTL: 쿠폰 만료 날짜

쿠폰의 재고 수량은 Strings 자료구조를 활용해서 관리하도록 설계했습니다.  
쿠폰이 발급될 때마다 `DECR` 명령어를 통해 1씩 감소하게 되는 구조입니다.

### 사용자 보유 쿠폰

- Redis 자료구조: Sets
- Key 패턴: `coupon:issued:{couponId}`
- Members: `{customerId}`
- TTL: 쿠폰 만료 날짜

사용자 보유 쿠폰은 Sets 자료구조를 활용해, 중복되는 사용자가 등록될 수 없도록 했습니다.  
이를 통해 특정 사용자가 쿠폰 발급 요청을 할 때, `SISMEMBER` 명령어를 통해 빠르게 중복 발급 여부를 확인할 수 있습니다.

### 쿠폰 발급 기능 설계

선착순 쿠폰 발급 기능은 Redis 기반으로 원자성 및 동시성 제어를 보장할 수 있도록 Lua Script 위주로 설계했습니다.  
Lua Script 로직의 흐름은 다음과 같습니다.

1. 사용자의 쿠폰 중복 발급 여부 확인
2. 남은 쿠폰 수량 확인
3. 쿠폰 수량 차감
4. 사용자게에 쿠폰 발급

위 Lua Script 기반의 로직은 Redis의 단일 스레드 특성을 활용해, **Race Condition** 없이 일관성 있는 처리를 보장할 수 있습니다.  
Redis 기반의 쿠폰 발급 로직을 수행한 이후에는 발급 내역을 DB에 동기화하는 작업을 순차적으로 진행합니다.

### 신규 쿠폰 생성 관련 사항

우리 서비스에서 현재로서는 쿠폰을 생성하는 Admin API가 따로 제공되고 있지는 않습니다.  
하지만 차후 기능 요구사항 확장에 따라 쿠폰 생성 관련 기능이 필요해질 경우, 데이터베이스에 쿠폰을 추가하는 것 외에도 아래의 명령을 추가로 실행해주어야 합니다.

- `SET coupon:stock:{couponId} 100`
- `EXPIRE coupon:stock:{couponId} {coupon-expired-at}`

<br>

## 3. 구현 세부사항

### Lua Script

```lua
-- KEYS[1] = coupon:stock:{couponId}
-- KEYS[2] = coupon:issued:{couponId}
-- ARGV[1] = customerId
-- ARGV[2] = TTL seconds

-- 사용자의 쿠폰 보유 여부 확인
if redis.call("SISMEMBER", KEYS[2], ARGV[1]) == 1 then
    return -2
end

-- 쿠폰 남은 수량 확인
local stock = redis.call("GET", KEYS[1])
if not stock then
    return -1
end
if tonumber(stock) <= 0 then
    return -3
end

-- 쿠폰 차감
redis.call("DECR", KEYS[1])

-- 사용자에게 쿠폰 발급
if redis.call("EXISTS", KEYS[2]) == 0 then
    redis.call("SADD", KEYS[2], ARGV[1])
    redis.call("EXPIRE", KEYS[2], ARGV[2])
else
    redis.call("SADD", KEYS[2], ARGV[1])
end

return 1
```

- 쿠폰 중복 발급 여부 체크 → 쿠폰 ID 체크 → 쿠폰 수량 체크 → 쿠폰 차감 → 쿠폰 발급의 순서로 진행
- 쿠폰 발급 유효성 검사에 실패했을 경우 에러 코드 반환
  - `1` : 쿠폰 정상 발급
  - `-1` : 쿠폰이 존재하지 않음
  - `-2` : 사용자가 이미 해당 쿠폰을 보유하고 있음
  - `-3` : 쿠폰 수량 부족

### Service 계층 로직 구현

```kotlin
fun issueWithRedis(coupon: Coupon, customerId: Long) {
    val script = luaScriptRegistry.getScript(LuaScriptId.COUPON_ISSUE, Long::class.java)
    val now = LocalDateTime.now()
    val expireAt = coupon.expiredAt.atStartOfDay()
    val ttl = Duration.between(now, expireAt).coerceAtLeast(Duration.ZERO)

    val resultCode = redisRepository.executeWithLua(
        script,
        keys = listOf(
            "coupon:stock:${coupon.id}",
            "coupon:issued:${coupon.id}"
        ),
        args = listOf(
            customerId.toString(),
            ttl.seconds.toString()
        )
    )

    val result = CouponInfo.IssueResult.fromCode(resultCode)

    when (result) {
        CouponInfo.IssueResult.SUCCESS -> return
        CouponInfo.IssueResult.NON_FOUND -> throw CouponNotFoundException()
        CouponInfo.IssueResult.ALREADY_ISSUED -> throw CustomerCouponAlreadyIssuedException()
        CouponInfo.IssueResult.INSUFFICIENT -> throw CouponInsufficientException()
        else -> throw CouponIssueFailedException()
    }
}
```

- `coupon-issue.lua` 파일을 불러와서 스크립트를 실행하는 방식
- **쿠폰 재고 키**, **쿠폰 발급 키**, **사용자 ID**, **TTL 값**을 인자로 넣고 스크립트 실행
- 스크립트 실행 결과에 따라 성공 처리 혹은 예외 처리 수행

### 응용 계층 로직 변경

**AS-IS**

```kotlin
@Transactional
@DistributedLock(
    resourceName = "couponId",
    key = "#command.couponId",
    lockType = LockType.SPIN,
    fallbackToDatabaseLock = true
)
fun issueCouponToCustomer(command: CouponCommand.Issue): CouponResult.Issue {
    val customer = customerService.getById(command.customerId)
    val coupon = couponService.getByIdWithLock(command.couponId)

    // 쿠폰 사용 가능 기간 검사
    coupon.validatePeriod()

    // 쿠폰 수량 검사 및 차감
    couponService.decreaseQuantity(coupon)

    // 쿠폰 발급
    val customerCoupon = customerCouponService.issue(customer, coupon)
    return CouponResult.Issue.from(customerCoupon)
}
```

- Redis 분산 락과 함께, 내부적으로 DB 비관적 락(`getByIdWithLock`)이 걸려 있는 상황
- 동시성 제어 측면에서 굉장히 안정적인 구조이지만, 트래픽이 몰릴 경우 심각한 지연을 발생시킬 수 있으며 느린 발급 실패 처리로 인해 불쾌한 UX를 제공하게 될 수 있음

**TO-BE**

```kotlin
@Transactional
fun issueCouponToCustomer(command: CouponCommand.Issue): CouponResult.Issue {
    val (couponId, customerId) = command
    val customer = customerService.getById(customerId)
    val coupon = couponService.getById(couponId)

    // 쿠폰 사용 가능 기간 검사
    coupon.validatePeriod()

    // Redis를 활용한 쿠폰 발급 처리
    couponService.issueWithRedis(coupon, customerId)

    // 쿠폰 수량 검사 및 차감
    couponService.decreaseQuantity(coupon)

    // 쿠폰 발급
    val customerCoupon = customerCouponService.issue(customer, coupon)
    return CouponResult.Issue.from(customerCoupon)
}
```

- 분산 락 및 DB 락을 제거하고, Lua Script를 활용해서 원자성을 보장하는 전략 채택
- 이로써 Redis의 빠른 조회 성능을 통해 **Fail-Fast 전략**을 활용할 수 있게 됨

<br>

## 4. 추가로 고려해볼 만한 사항들

### DB 동기화 방식의 비동기 전환

현재 쿠폰 발급 기능 구현사항은 Redis를 활용한 쿠폰 발급 성공 이후, DB에 동기적으로 변경사항을 저장하고 있습니다.  
그렇기 때문에 쿠폰 발급 요청이 몰릴 경우 DB 병목 현상이 발생할 수 있다는 약점이 존재합니다.  
따라서 향후 Kafka, Redis Stream, MQ 등의 **이벤트 기반 기술**을 도입한다면, DB 동기화 작업을 비동기 처리로 구현할 수 있을 것입니다.  
다만 이벤트 기반 비동기 기능을 구현할 경우, 실패 시 재처리 로직이나 알림 전략 등에 대해서 함께 고려되어야 할 것입니다.
