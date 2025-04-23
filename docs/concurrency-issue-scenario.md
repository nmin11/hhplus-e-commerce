# 동시성 이슈 해결 전략

## 서론

항해 플러스 백엔드 과정 2주차 ~ 4주차까지 구현된 E-커머스 서비스의 기능들은 기본적인 구현 요구사항들에 충실하도록 개발되었습니다.  
하지만 현 상태에서는 동시성 요청에 대한 방지책이 미비하므로, 치명적인 동시성 문제가 발생할 수 있는 상태입니다.  
그러므로 본 보고서를 작성함으로써 발생 가능한 동시성 이슈들을 명확히 규정하고, 이를 해결하기 위해 거쳤던 과정을 기술하고자 합니다.

<br>

## 동시성 이슈 시나리오 선정

| Lock 대상 테이블 | 시나리오 키워드 | 예상 문제 | 적용한 Lock 방식 |
|:------------------:|:----------------------:|:-------------------------:|:-----------------------------:|
| `balance`           | 사용자 잔액 동시 차감 | 잔액 음수 저장 가능성          | **Pessimistic Write Lock** |
| `balance`           | 사용자 잔액 동시 충전 | 일부 금액 누락 가능성            | **Optimistic Lock + Retry** |
| `coupon`            | 쿠폰 수량 동시 차감   | 수량 초과 발급 가능성            | **Pessimistic Write Lock** |
| `stock`             | 재고 동시 차감        | 재고 음수 발생 가능성            | **Pessimistic Write Lock** |
| `order`             | 주문 상태 전이        | 이중 결제 발생 가능성            | **Optimistic Lock**     |
| `customer_coupon`   | 쿠폰 상태 전이        | 쿠폰 중복 사용 가능성            | **Optimistic Lock**     |

본 프로젝트에서 발생 가능한 동시성 이슈들을 위와 같이 표로 정리했습니다.  
잔액 차감, 재고 차감, 쿠폰 발급 같이 서비스의 핵심 자원에 대한 차감 로직은 정합성 및 원자성이 보장되어야 한다고 판단해 **비관적 쓰기 락** 방식을 적용했습니다.  
반면에 객체의 상태 값을 참조하는 로직이 있는 경우에는 **낙관적 락**을 활용해 상태 전이가 정확히 한 번만 일어나도록 적용했습니다.  
잔액 충전의 경우에는 반드시 정합성과 원자성을 보장하기 보다는, 재시도가 가능한 로직이라고 판단하여 **낙관적 락 + 재시도 로직**의 방식을 활용했습니다.

<br>

## 사용자 잔액 동시 차감

### 문제 식별

사용자 잔액 차감 로직은 `잔액 >= 요청 금액` 여부를 검사한 후 잔액을 차감하는 방식입니다.  
하지만 여러 트랜잭션에서 동시 요청이 들어올 경우, 각자 차감 작업이 유효하다고 판단해 동시에 차감할 수 있습니다.  
이에 따라 사용자의 잔액이 음수값이 될 수 있는 치명적인 문제가 존재합니다.

### 분석 (AS-IS)

```kotlin
@Transactional
fun deduct(customerId: Long, amount: Int): Balance {
    val balance = getByCustomerId(customerId)
    balance.deduct(amount)
    return balanceRepository.save(balance)
}
```

- `getByCustomerId` 메서드로 조회된 Balance 객체는 별도의 Lock 없이 여러 트랜잭션에서 동시 접근 가능
- 여러 트랜잭션이 동시에 차감 요청을 할 경우, 잔액이 부족함에도 차감이 모두 성공할 가능성이 있음

### 해결 (TO-BE)

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT b FROM Balance b WHERE b.customer.id = :customerId")
fun findByCustomerIdWithLock(customerId: Long): Balance?
```

```kotlin
private fun getByCustomerIdWithLock(customerId: Long): Balance {
    return balanceRepository.findByCustomerIdWithLock(customerId)
        ?: throw IllegalStateException("잔액 정보가 존재하지 않습니다.")
}
```

- `getByCustomerId` 메서드 대신 `getByCustomerIdWithLock` 메서드를 활용하도록 변경
- **비관적 쓰기 락**을 통해 잔액 차감의 정합성 보장
- 이에 따라 여러 트랜잭션이 동시에 차감 요청을 해도 Lock이 걸려서 직렬화 처리됨

<br>

## 사용자 잔액 동시 충전

### 문제 식별

여러 트랜잭션애서 동시에 충전 요청을 할 경우, 각 트랜잭션이 동일한 시점의 잔액을 조회하고 충전 금액을 연산한 뒤 저장할 수 있습니다.  
이로 인해 충전 요청 금액의 일부가 유실될 수 있다는 동시성 문제가 존재합니다.

### 분석 (AS-IS)

```kotlin
fun charge(customerId: Long, amount: Int): Balance {
    val balance = getByCustomerId(customerId)
    balance.charge(amount)
    return balanceRepository.save(balance)
}
```

```kotlin
fun charge(amount: Int) {
    require(amount > 0) { "충전 금액은 0보다 커야 합니다." }
    this.amount += amount
}
```

- 여러 트랜잭션이 동시에 같은 `amount` 값을 조회하고 충전 연산을 수행할 가능성이 있음
- 이에 따라 충전 요청 중 일부만 반영되고, 나머지 충전 요청은 누락되는 현상이 발생할 수 있음

### 해결 (TO-BE)

```kotlin
@Version
var version: Long = 0L
```

```kotlin
@Retryable(
    value = [ObjectOptimisticLockingFailureException::class],
    maxAttempts = 2,
    backoff = Backoff(delay = 100)
)
fun charge(customerId: Long, amount: Int): Balance {
    val balance = getByCustomerId(customerId)
    balance.charge(amount)
    return balanceRepository.saveAndFlush(balance)
}

@Recover
fun recoverCharge(e: ObjectOptimisticLockingFailureException, customerId: Long, amount: Int): Balance {
    log.warn("충전 재시도 실패: customerId=$customerId, amount=$amount, message=${e.javaClass.simpleName}")
    throw IllegalStateException("지금은 충전을 진행할 수 없습니다. 잠시 후 다시 시도해주세요.")
}
```

- `version` 필드와 함께 **낙관적 락** 적용
- 충돌 시 1번의 재시도를 수행하도록 적용
- 고객 잔액 조회 등에 대한 side-effect를 최대한 덜면서도, 경합 상황을 감지하고 1회 재시도 로직과 함께 처리할 수 있도록 구현됨

<br>

## 쿠폰 수량 동시 차감

### 문제 식별

다수의 사용자가 하나의 쿠폰에 대한 발급 요청을 동시에 할 경우, 서로 동일한 수량 상태의 쿠폰을 조회할 가능성이 존재합니다.  
이에 따라 쿠폰 수량이 부족함에도 쿠폰 발급 로직이 실행되어서 실제 쿠폰 수량보다 더 많은 수의 쿠폰이 발급될 수 있습니다.

### 분석 (AS-IS)

```kotlin
fun decreaseQuantity() {
    if (currentQuantity <= 0) {
        throw IllegalStateException("쿠폰 수량이 모두 소진되었습니다.")
    }

    currentQuantity -= 1
}
```

- 여러 트랜잭션이 동시에 동일한 `currentQuantity` 값을 조회하고 차감 연산을 진행할 수 있음
- 이에 따라 쿠폰 `totalQuantity`를 넘어서도록 초과 발급이 발생할 수 있음

### 해결 (TO-BE)

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Coupon c WHERE c.id = :id")
fun findByIdWithLock(id: Long): Coupon
```

- 발급 대상 쿠폰에 대한 **비관적 쓰기 락** 적용
- 한 트랜잭션이 쿠폰을 발급받고 있다면 다른 트랜잭션은 락이 해제될 때까지 대기
- 이에 따라 `currentQuantity <= 0` 검사 시점에 대한 정합성 부여

<br>

## 재고 동시 차감

### 문제 식별

재고가 한정되어 있는 상품에 대해 여러 사용자가 동시에 결제할 경우, 각 트랜잭션이 동일한 재고 수량을 기반으로 차감 연산을 진행할 가능성이 있습니다.  
이에 따라 재고 수량 값이 음수로 저장되는 동시성 문제가 발생할 수 있습니다.

### 분석 (AS-IS)

```kotlin
@Transactional
fun decrease(productOptionId: Long, quantity: Int) {
    val stock = getByProductOptionId(productOptionId)
    stock.decrease(quantity)
    stockRepository.save(stock)
}
```

- `getByProductOptionId` 메서드를 통해 여러 트랜잭션이 동일한 Stock 객체를 조회할 수 있음
- 이에 따라 실제 재고가 충분하지 않음에도 차감이 이루어져서 재고 수량이 음수 값이 될 수 있음

### 해결 (TO-BE)

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Stock s WHERE s.productOption.id = :productOptionId")
fun findByProductOptionIdWithLock(productOptionId: Long): Stock?
```

- **비관적 쓰기 락**을 적용해 상품의 재고가 정합성을 갖추도록 변경
- 재고 부족 여부 검사 및 차감 로직이 트랜잭션 내에서 원자적으로 보장되도록 구현

<br>

## 주문 상태 전이

### 문제 식별

결제 로직에서는 주문 상태가 `CREATED`인 경우에만 결제가 가능하도록 구현되어 있습니다.  
하지만 여러 트랜잭션이 동시에 요청할 경우 `CREATED` 상태의 주문을 조회하고, 이에 따라 중복으로 결제가 발생할 가능성이 있습니다.

### 문제 (AS-IS)

```kotlin
fun getValidOrderForPayment(orderId: Long): Order {
    val order = getById(orderId)
    if (order.status != OrderStatus.CREATED) {
        throw IllegalStateException("결제 가능한 주문이 아닙니다. (현재 상태: ${order.status})")
    }
    return order
}
```

- 결제 로직 내부의 주문 조회 로직은 주문 상태가 `CREATED`인지 조회하고 반환
- 하지만 여러 트랜잭션이 동시에 메소드를 호출할 경우 동일한 `CREATED` 상태를 조회하고 동시에 결제를 진행하게 될 우려가 있음

### 해결 (TO-BE)

```kotlin
@Version
@Column(nullable = false)
var version: Long = 0L
```

```kotlin
fun markAsPaid(order: Order) {
    try {
        order.markAsPaid()
        orderRepository.saveAndFlush(order)
    } catch (_: ObjectOptimisticLockingFailureException) {
        throw IllegalStateException("지금은 결제를 진행할 수 없습니다. 잠시 후 다시 시도해주세요.")
    }
}
```

- `version` 필드 기반의 **낙관적 락 적용**
- 별도의 Retry 로직 없이 특정 주문에 대한 결제는 반드시 1건만 성공할 수 있도록 구현
- Order 객체의 flush 시점을 명확히 하여 `ObjectOptimisticLockingFailureException` 예외 시점을 정확히 잡아내도록 구현

<br>

## 쿠폰 상태 전이

### 문제 식별

사용자에게 발급된 쿠폰은 `AVAILABLE` 상태일 때 사용 가능합니다.  
하지만 한 사용자가 여러 결제에서 동일한 쿠폰을 사용하려고 할 때, 하나의 쿠폰이 중복으로 사용될 가능성이 있습니다.

### 분석 (AS-IS)

```kotlin
fun validateIssuedCoupon(customerId: Long, couponId: Long): CustomerCoupon {
    val customerCoupon = customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
        ?: throw IllegalArgumentException("해당 쿠폰은 고객에게 발급되지 않았습니다.")

    return customerCoupon.validateUsable()
}
```

```kotlin
fun validateUsable(): CustomerCoupon {
    checkValidation()
    return this
}

private fun checkValidation() {
    if (status == CustomerCouponStatus.USED) {
        throw IllegalStateException("이미 사용된 쿠폰입니다.")
    }
    if (status == CustomerCouponStatus.EXPIRED) {
        throw IllegalStateException("사용 기간이 만료된 쿠폰입니다.")
    }
}
```

- 동일한 쿠폰을 사용하는 여러 결제를 요청할 경우, 동시에 `AVAILABLE` 상태의 쿠폰을 조회하고 할인을 적용할 가능성이 있음
- 이에 따라 하나의 쿠폰이 여러 결제에서 사용될 수 있음

### 해결 (TO-BE)

```kotlin
@Version
var version: Long = 0L
```

```kotlin
fun markAsUsed(customerCoupon: CustomerCoupon) {
    try {
        customerCoupon.markAsUsed()
        customerCouponRepository.saveAndFlush(customerCoupon)
    } catch (_: ObjectOptimisticLockingFailureException) {
        throw IllegalStateException("지금은 결제를 진행할 수 없습니다. 잠시 후 다시 시도해주세요.")
    }
}
```

- `version` 필드 기반의 **낙관적 락 적용**
- 별도의 Retry 로직 없이 쿠폰 사용은 반드시 1번만 수행할 수 있도록 구현
- CustomerCoupon 객체의 flush 시점을 명확히 하여 `ObjectOptimisticLockingFailureException` 예외 시점을 정확히 잡아내도록 구현

<br>

## 동시성 제어 전략 요약 및 대안

본 프로젝트에서는 서비스 내부의 주요 동시성 문제들을 해결하기 위해 **DB 기반의 동시성 제어 방식**을 적용했습니다.  
제가 선택했던 동시성 제어 전략은 다음과 같습니다.

- 비관적 쓰기 락
  - 사용자 잔액, 재고, 쿠폰 수량은 한정된 핵심 자원이므로, 차감하는 로직들은 정합성 및 원자성을 보장해야 함
  - 요청을 직렬화해서 동시 접근을 순차적으로 처리하도록 구현
- 낙관적 락
  - 주문, 사용자 쿠폰은 상태 값이 정확히 한 번만 변경되어야 함 (상태 전이)
  - 그러므로 충돌을 감지해서 한 번만 처리하도록 보장하되, 읽기 성능에 대한 side-effect를 주지 않도록 구현
- 낙관적 락 + 재시도 로직
  - 사용자 잔액 충전의 경우 정합성은 중요하지만 경합 빈도는 낮음
  - 제한된 재시도(1회) 로직을 활용해 사용자 경험과 성능의 균형을 고려함

저만의 판단에 근거하여 동시성 제어 로직들을 위와 같이 구현했습니다.  
하지만 이는 비즈니스적 판단 근거에 따라 충분히 달라질 수 있는 영역이라고 생각합니다.  
이를테면 쿠폰의 수량이 무수히 많고 하나하나의 쿠폰이 그다지 소중한 자원이 아닌 경우의 수도 있을 것이고, 또 사용자의 잔액 충전이 현금 결제와 밀접한 연관이 생겨서 더욱 엄밀하게 안정성을 고려해야 할 수도 있을 것입니다.

더 나아가서, 앞으로 서비스를 멀티 인스턴스 환경으로 확장한다면 DB 수준의 락만으로는 동시성 제어에 한계가 있을 것입니다.  
이 때는 아래와 전략에 대해 추가적으로 고려해봐야 할 것입니다.

- Redis 기반의 분산 락
- Kafka/Stream 기반의 비동기 처리 시스템
