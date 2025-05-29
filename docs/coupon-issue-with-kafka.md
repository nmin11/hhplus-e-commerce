# Kafka를 활용한 쿠폰 발급 대용량 트래픽 처리 설계

## 1. 설계 방향

기존에 진행했던 [Redis 기반의 쿠폰 발급 기능 설계](https://github.com/nmin11/hhplus-e-commerce/wiki/Redis-%EA%B8%B0%EB%B0%98-%EC%BF%A0%ED%8F%B0-%EB%B0%9C%EA%B8%89-%EA%B8%B0%EB%8A%A5-%EC%84%A4%EA%B3%84-%EB%B0%8F-%EA%B0%9C%EB%B0%9C-%ED%9A%8C%EA%B3%A0) 작업에서는 Redis와 Lua Script를 활용해서 빠르게 쿠폰 발급 요청을 선별하고, 조건에 맞지 않는 요청은 즉시 실패시키는 **Fail-Fast 전략**을 기반으로 병목 현상을 줄일 수 있었습니다.

이 방식은 앞단에서의 요청 선별 작업에는 효과적이었지만, 발급된 쿠폰을 DB에 반영하는 후속 작업에서 발생할 수 있는 병목 지점에 대해서는 충분히 고려되지 않았습니다.  
특히 이벤트성 선착순 쿠폰 발급 기능은 짧은 시간이 수많은 요청이 집중되는 **스파이크 트래픽**이 자주 발생하기 때문에 DB 쓰기 부하가 폭발적으로 증가하고, 이에 따라 성능 저하 및 장애로 이어질 가능성이 존재합니다.

이를 해결하고자 **Kafka 이벤트 기반의 처리 구조**를 도입하게 되었습니다.  
이번 설계에서는 Kafka를 도입함으로써 다음과 같은 이점들을 챙겨가고자 합니다.

- **버퍼링**: Kafka는 높은 처리량을 지원하는 분산 스트리밍 플랫폼이기 때문에, 순간적인 트래픽 급증에도 요청을 안정적으로 버퍼링할 수 있습니다.
- **확장성**: Kafka 클러스터는 수평 확장이 가능하기 때문에 프로모션 이벤트의 성격에 따라 유연하게 조절이 가능합니다.
- **배치 처리**: Kafka에서는 레코드를 모았다가 한번에 처리하는 방식을 활용할 수 있기 때문에 잦은 커넥션 요청 대신에 한번에 많은 요청을 처리하도록 할 수 있습니다.
- **at-least-once 처리**: 모든 메시지에 대해 최소 한 번의 처리가 반드시 보장됩니다.

<br>

## 2. 설계 세부사항

### Event Producing

- 발행 시점: Redis를 통한 쿠폰 발급 성공 직후 (`couponService.issue()` 이후)
- 이벤트 이름: `CouponIssuedEvent`
- Topic 이름: `inside.coupon.issued`

### Event Consuming

- Consuming 대상 Topic: `inside.coupon.issued`
- Consumer Group ID: `coupon-service-group`
- 수신 이벤트 타입: `List<CouponIssuedEvent>`
- 배치 최대 레코드 수: 100개
- 배치 최대 대기시간: 500ms
- offset 커밋 방식: manual

### 이벤트 처리 흐름

1. Redis Lua Script를 활용한 쿠폰 발급 처리
2. 발급 성공 이후 `CouponIssuedEvent` 발행 → 쿠폰 발급 완료 응답 반환
3. Kafka Listener를 통해 이벤트 수신 및 DB 배치 반영

쿠폰 발급의 핵심 로직은 Lua Script 기반 Redis 로직에서 수행됩니다.  
Redis에서의 발급 단계가 성공적으로 완수된 이후에는 Kafka를 통해 `CouponIssuedEvent`를 발행하며, 곧바로 발급 완료 응답을 먼저 반환합니다.  
쿠폰 발급 완료 이벤트는 Kafka에서 최대 레코드 100개 혹은 최대 대기시간 500ms를 채울 때까지 대기하고, 이후에 배치로 DB 반영 로직을 수행합니다.  
이벤트를 소비하는 Kafka Listener에서는 모든 이벤트 처리 및 DB 반영 완료 후 offset 수동 커밋을 진행합니다.

### Sequence Diagram

**AS-IS**

![coupon-issue-origin-sequence](./coupon-issue-with-kafka/coupon-issue-origin-sequence.drawio.svg)

기존 쿠폰 발급 로직은 Redis를 활용한 쿠폰 발급 수행 이후에도 곧바로 DB에 발급 내역을 반영하는 로직까지 순차적으로 실행하고 있었습니다.  
이로 인해 다수의 요청이 동시에 몰릴 경우 DB 커넥션을 열고 닫는 작업이 반복되기 때문에 **커넥션 풀 고갈**, **쓰기 부하 및 응답 지연**의 문제가 발생할 수 있었습니다.

**TO-BE**

![coupon-issue-sequence](./coupon-issue-with-kafka/coupon-issue-sequence.drawio.svg)

이번 설계에서는 Redis를 통한 쿠폰 발급 성공 직후 Kafka 메시지를 발행하고, 메인 로직에서는 더 이상 DB 반영을 기다리지 않고 즉시 응답을 반환하는 구조로 변경되었습니다.  
이로 인해 쿠폰 발급에 대한 DB 반영 작업은 Kafka Consumer가 비동기적으로 처리하며, 또한 메시지는 배치로 처리되기 때문에 잦은 커넥션 연결 및 해제 없이 보다 안정적으로 쿠폰 변경 내역을 일괄 반영할 수 있게 되었습니다.

이러한 변경점으로 인해 다음과 같은 효과를 기대할 수 있습니다.

- **사용자 응답 시간 단축**: 쿠폰 발급 내역에 대한 DB 반영 작업까지 기다리지 않기 때문에 요청에 대한 응답 속도가 빨라질 것
- **DB 커넥션 풀 관리**: DB 커넥션을 보다 효율적으로 사용함으로써 커넥션 풀 고갈 위험 완화
- **DB 쓰기 부하 조절**: 트래픽이 급증해도 Kafka 기반의 병렬 처리로 인해 DB 쓰기 부하가 급격히 증가하지 않음

<br>

## 3. 구현 세부사항

### 쿠폰 발급 완료 이벤트

```kotlin
data class CouponIssuedEvent(
    val couponId: Long,
    val customerId: Long
)
```

- Redis에서 쿠폰 발급을 마치고 전송할 이벤트 객체
- 필요한 최소 정보만 담을 수 있도록 설계하였음

### Event 발급 로직

```kotlin
@Component
class KafkaCouponEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : CouponEventPublisher {
    override fun publish(event: CouponIssuedEvent) {
        kafkaTemplate.send(
            "inside.coupon.issued",
            event.couponId.toString(),
            event
        )
    }
}
```

- `inside.coupon.issued` 토픽에 `{couponId}`를 키 값으로 하는 이벤트 발송

### Event Consume 로직

```kotlin
@Transactional
@KafkaListener(topics = [TOPIC_NAME], groupId = GROUP_ID, batch = "true")
fun listen(events: List<CouponIssuedEvent>, ack: Acknowledgment) {
    log.info("[Kafka] [Coupon] 쿠폰 발급 메시지 수신: size=${events.size}")

    if (events.isEmpty()) return

    val groupedByCouponId = events.groupBy { it.couponId }
    val couponMap = groupedByCouponId.mapValues { (couponId, _) ->
        couponService.getById(couponId)
    }

    // 쿠폰 수량 차감
    groupedByCouponId.forEach { (couponId, groupEvents) ->
        val coupon = couponMap.getValue(couponId)
        couponService.decreaseQuantity(coupon, groupEvents.size)
    }

    // 고객에게 쿠폰 발급
    val customerCoupons = events.map {
        val coupon = couponMap.getValue(it.couponId)
        val customer = customerService.getById(it.customerId)
        CustomerCoupon.issue(customer, coupon)
    }
    customerCouponService.issueAll(customerCoupons)

    ack.acknowledge()
}
```

배치 처리를 위해 이벤트를 배열로 받고 있습니다.  
경우에 따라서는 쿠폰 프로모션 이벤트가 여러 건 진행될 수 있으므로, 각 쿠폰에 대한 Grouping 처리가 필요합니다.

1. 쿠폰 기준으로 이벤트 Grouping
2. 그룹별 쿠폰 수량 차감 (쿠폰 당 1건의 `update` 쿼리)
3. CustomerCoupon 객체 생성 및 일괄 생성 (`insert`)
4. 메시지 수동 커밋

### 응용 계층 로직 변경

**AS-IS**

```kotlin
@Transactional
fun issueCouponToCustomer(command: CouponCommand.Issue): CouponResult.Issue {
    val (couponId, customerId) = command
    val customer = customerService.getById(customerId)
    val coupon = couponService.getById(couponId)

    // 쿠폰 사용 가능 기간 검사
    coupon.validatePeriod()

    // Redis를 활용한 쿠폰 발급 처리
    couponService.issue(coupon, customerId)

    // 쿠폰 수량 검사 및 차감
    couponService.decreaseQuantity(coupon)

    // 쿠폰 발급
    val customerCoupon = customerCouponService.issue(customer, coupon)
    return CouponResult.Issue.from(customerCoupon)
}
```

- Redis 기반의 쿠폰 발급 이후에 DB 반영까지 한 이후에 발급 결과를 응답하고 있는 상황
- 발급 실패는 Redis 기반으로 Fail-Fast하게 처리할 수 있으나, 발급 성공 케이스가 한번에 몰리면 DB 부하로 인한 장애가 발생할 수 있음

**TO-BE**

```kotlin
fun issueCouponToCustomer(command: CouponCommand.Issue): CouponResult.Issue {
    val (couponId, customerId) = command

    // Redis를 활용한 쿠폰 발급 처리
    couponService.issue(couponId, customerId)

    // 쿠폰 발급 이벤트 발송
    couponEventPublisher.publish(
        CouponIssuedEvent(couponId, customerId)
    )

    return CouponResult.Issue(couponId, customerId)
}
```

- DB 관련 로직이 없어졌으므로 `@Transactional` 분리
- 쿠폰 사용 가능 기간 검증도 Redis 로직으로 추가
- Redis 쿠폰 발급 처리 이후 발급 완료 이벤트 `CouponIssuedEvent`만 발송하고 발급 완료 응답 반환
- 이로써 사용자는 DB 반영에 대한 응답까지 기다릴 필요가 없어졌으며, DB 처리 로직 또한 매 요청마다 발생하지 않기 때문에 DB 쓰기 부하를 줄였음

<br>

## 4. 아쉬운 점들

### 메시지 유실에 대한 처리

현재 Kafka를 활용한 쿠폰 발급 시스템은 Kafka의 높은 내결함성(Fault-Tolerance)을 신뢰한다는 전제 하에 구성되었으며, 이에 따라 메시지 유실 가능성에 대한 보완이 미비한 상태입니다.  
하지만 우리 서비스가 장기적으로 운영됨에 따라 예기치 못한 장애(네트워크 단절, Kafka 클러스터의 결함)가 발생하는 상황을 완전히 배제할 수는 없게 될 것입니다.

지금 설계된 쿠폰 발급 기능 DB 연동 부분 또한, 메시지가 유실될 경우 사용자가 쿠폰을 실제 결제에 활용하지 못하게 되는 심각한 UX 저하 경험을 불러일으킬 수 있습니다.  
따라서 '유실된 메시지를 처리하는 작업'을 높은 우선순위의 차기 작업으로 두어야 할 것으로 판단됩니다.

메시지 유실을 방지하기 위해, 다음과 같은 작업들을 단계적으로 수행해야 할 것입니다.

- Producer 측에서의 안정적인 메시지 발행 보장

메시지 발행 자체가 실패하는 경우에 대비하기 위해 **Outbox 패턴** 또는 **CDC(Change Data Capture)** 구조를 도입해야 할 것으로 보입니다.  
Outbox 패턴으로 이벤트 발행 이전에 해당 이벤트를 저장하고, CDC 도구가 Outbox 테이블의 변경사항을 감지해서 Kafka로 전송하는 방식으로 Producer의 메시지 발행을 보장할 수 있을 것입니다.

- Consumer의 지속 실패에 대한 보완

현재 Kafka Consumer는 메시지를 수신한 후 처리 도중 예외가 발생하면 offset에 대한 수동 커밋 작업`acknowledge()`을 생략하게 되어 해당 메시지에 대한 처리를 재시도하게 됩니다.  
하지만 재시도가 반복적으로 실패하는 경우 Consumer의 처리 흐름을 지연시킬 수 있으므로 이를 보완하기 위한 전략이 필요합니다.

대표적인 방식으로는 **DLQ(Dead Letter Queue)** 가 있습니다.  
일정 횟수 이상 실패한 메시지를 DLQ Topic으로 이동시켜 메인 Consumer와 분리시키고, DLQ에 있는 메시지는 별도의 배치 작업 등으로 나중에 처리하도록 할 수 있을 것입니다.

### 메시지 소비의 멱등성 문제

```kotlin
@Transactional
@KafkaListener(batch = "true")
fun listen(events: List<CouponIssuedEvent>, ack: Acknowledgment) {

    // 쿠폰 수량 차감

    // 고객에게 쿠폰 발급

    ack.acknowledge()
}
```

쿠폰 발급 내역을 DB에 반영하는 부분은 위와 같이 쿠폰 수량 차감 및 고객 쿠폰 발급을 DB에 처리합니다.  
위 로직은 처리 도중 예외가 발생해도 트랜잭션이 롤백되고, offset 커밋도 진행되지 않을 것이기 때문에, 메시지를 다시 처리하더라도 기존 변경사항에 추가로 덮어쓰는 이슈는 발생하지 않도록 기본적으로는 보장하고 있습니다.

하지만 문제는 '쿠폰 수량 차감' 부분의 로직이 멱등하지 않다는 데에 있습니다.  
사용자 보유 쿠폰은 DB 상의 UNIQUE 제약 조건으로 인해 멱등하게 동작하지만, 쿠폰 수량의 경우 같은 메시지를 중복으로 처리하게 되었을 때 실제보다 더 많은 수량을 차감할 수 있다는 위험요소가 존재합니다.  
이는 추후 Transactional Outbox 패턴 등을 도입해서, **이벤트 저장 및 발행 모델**을 활용하게 될 경우 시스템의 신뢰성을 떨어뜨리는 악영향을 미칠 수 있습니다.  
따라서 쿠폰 수량 차감 로직에 대한 구조적 개선 작업이 '메시지 유실 처리'에 대한 작업보다 앞서 진행되어야 할 것으로 보입니다.

### Kafka 인프라 운영 난이도

이번 설계에서 Kafka는 쿠폰 발급 이벤트의 대용량 트래픽을 안정적으로 처리하기 위한 핵심 인프라로 사용되었습니다.  
하지만 Kafka를 도입함으로써 **인프라 운영의 복잡도 및 유지 비용이 증가했다는 점**은 명확한 트레이드오프라고 볼 수 있습니다.

Kafka는 분명히 대용량 트래픽을 높은 가용성 및 안정성과 함께 빠르게 처리할 수 있는 유용한 분산 처리 시스템입니다.  
하지만 이 기술을 제대로 활용하기 위해서는 Kafka 클러스터 관리에 대한 높은 이해도와 함께 어떻게 튜닝해야 하는지도 상세히 파악하고 있어야 합니다.  
Kafka 시스템에 대한 높은 이해도가 있어야만 장애 및 성능 병목에 대해 올바르게 대처할 수 있을 것입니다.

현재로서는 단일 토픽과 적은 수의 파티션만 사용하는 단순한 구조이기 때문에 큰 부담은 없지만, 향후에 MSA 구조로 확장하고, 다양한 도메인 이벤트를 처리하기 위한 EDA로서의 Kafka 사용을 고려한다면 관리 포인트가 기하급수적으로 증가하게 될 것입니다.

따라서 전체 시스템에 Kafka를 도입하는 것은 단순히 성능 개선만을 목적으로 하기보다, 조직의 기술 운용 역량과 장애 대응 체계까지 고려한 현실적인 설계 판단이 필요한 지점입니다.  
지금은 쿠폰 발급 기능을 개선하기 위해 단발성으로 Kafka 기술을 적용했지만 이 기술의 활용을 점진적으로 더 넓혀갈 것인지, 이에 대한 운영 부담을 감당할 수 있는지에 대해 충분한 논의와 검토가 더 진행되어야 할 것입니다.
