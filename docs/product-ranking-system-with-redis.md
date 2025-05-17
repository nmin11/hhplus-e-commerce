# Redis 기반 인기 상품 랭킹 시스템 설계 및 개발 회고

## 1. 설계 방향

저는 이번 과제에서 **비즈니스 요구사항에 걸맞는 기술 설계 역량 도출**이 핵심적인 키워드라고 생각했습니다.  
그렇기 때문에 우선적으로, 우리의 E-커머스 서비스가 현재 어떤 상황에 처해 있는지, 어떠한 이유로 Redis 기술을 도입하고자 하는지를 명확하게 정의하고자 했습니다.  
저는 우리 서비스가 아직 상품 수와 사용자 수가 많지 않은 **초기 성장 단계**라고 판단했습니다.  
신규 서비스 특성상 오래된 상품 데이터는 많지 않기 때문에, **일간(실시간), 3일, 7일 랭킹**을 집중적으로 제공하는 것이 적합한 방식이라고 판단했습니다.

<br>

## 2. 설계 세부사항

### 날짜별 상품 랭킹

- Redis 자료구조: Sorted Sets
- Key 패턴: `product:sales:{yyyyMMdd}`
- Members: `{productId}`
- TTL 8일

매일 날짜별 상품 판매 통계를 Redis Sorted Sets에 저장하도록 구성했습니다.  
해당 날짜에 상품 판매 이벤트가 발생할 때마다 상품 ID를 `member`로, 상품 판매량을 `score`로 저장합니다.  
TTL은 8일로 설정하여, 스케줄러가 최근 3일 및 7일 간의 랭킹 집계를 할 수 있도록 설계했습니다.

### 당일 인기 상품 (실시간성)

- 조회 Key: `product:rank:1d:{yyyyMMdd}`
- Look Aside 방식의 조회
- TTL 5분

당일 인기 상품 랭킹은 수 분 단위의 **실시간성**을 보장하고자 했습니다.  
그러므로 미리 스케줄링된 캐시를 사용하는 대신에, 요청 시점에 **Look Aside + Write Around** 방식으로 캐시를 저장하고 꺼내오는 방식을 채택했습니다.  
짧은 캐시 주기를 적용함으로써 5분 단위로 최신성을 유지할 수 있도록 구현했습니다.

### 기간별 인기 상품 (3일, 7일)

- 조회 Key: `product:rank:3d:{yyyyMMdd}`, `product:rank:1w:{yyyyMMdd}`
- Scheduler를 활용한 갱신 방식
  - 날짜별 랭킹 데이터 집계 작업
- TTL 25시간

3일, 7일 단위의 인기 상품 랭킹은 **매일 자정에 실행되는 스케줄러**가 날짜별 상품 랭킹 데이터를 집계해둔 ZSet들에 대한 `ZUNIONSTORE` 작업을 수행하면서 갱신됩니다.  
매일 자정에 당일의 상품 판매량은 없는 상태일 것이기 때문에 UNION 작업의 대상으로 -1일 ~ -3일 단위, 혹은 -1일 ~ -7일 단위로 집계했습니다.  
캐시 전략으로는 **Read Through + Cache Warming** 방식을 활용했습니다.  
캐시는 하루 단위로 갱신되기 때문에 실시간성은 다소 부족할 수 있지만, 기간별로 의미 있는 랭킹 데이터를 안정적으로 제공할 수 있는 방안이라고 생각했습니다.  
추가적으로, 만에 하나 Cache Miss가 발생할 경우에는 요청 시점에 `ZUNIONSTORE` 작업을 통해 캐시를 갱신할 수 있도록 보완해두었습니다.

### 당일, 3일, 7일이 아닌 다른 요청이 들어온다면?

현재 상품 랭킹 API는 단순히 정해진 기간(1일, 3일, 7일)에만 대응하는 것이 아니라, 임의의 기간(예: 최근 5일, 1달 등)에 대해서도 유연하게 처리할 수 있도록 설계되어 있습니다.  
다른 기간들에 대한 조회는 비즈니스 핵심 요구사항이 아니지만, 미래의 변경에 대한 대응책을 마련해두는 것이 좋을 것이라 판단하여 관련 로직을 구현했습니다.

**7일 이내의 요청인 경우**

- 요청된 날짜 범위의 ZSet 키들을 `ZUNIONSTORE`로 조합한 뒤 캐시 작업
- TTL 1시간

**7일을 초과하는 기간인 경우**

- 해당 날짜 범위의 ZSet들을 모두 집계할 수 없기 때문에 DB 조회 후 캐시 작업
- TTL 1시간

이러한 요청은 실제 요구사항을 벗어나기 때문에 빈번하게 발생하지 않는 상황이라고 판단했습니다.  
그러므로 수 분 단위의 실시간성을 고려할 필요가 없다고 판단해 1시간 단위의 TTL을 적용했으며, 핵심적인 집계 대상으로 바라바고 있는 7일 이내의 데이터가 아닌 경우, DB에서 조회하는 것이 큰 무리가 아닐 것으로 판단했습니다.

<br>

## 3. 구현 세부사항

### 일일 상품 판매량 갱신

```kotlin
companion object {
    private val TTL = Duration.ofDays(8)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
fun handle(command: ProductCommand.SalesUpdated) {
    val today = dateFormatter.format(LocalDate.now())
    val redisKey = "product:sales:$today"

    command.items.forEach { item ->
        val productId = item.productId.toString()
        val quantity = item.quantity.toDouble()

        if (redisRepository.exists(redisKey)) {
            redisSortedSetRepository.incrementScore(redisKey, productId, quantity)
        } else {
            redisSortedSetRepository.add(redisKey, productId, quantity, TTL)
        }
    }
}
```

- 결제 로직이 완료된 후 이벤트를 받아서 상품 통계를 업데이트하는 방식
- 날짜별 상품 랭킹을 저장하는 Redis ZSet에 상품 ID를 `member`로, 상품 판매량을 `score`로 저장
- TTL을 8일로 지정하여, 일주일 간의 상품 랭킹 데이터를 집계할 수 있도록 구현

### 3일 및 7일 간 인기 상품 랭킹을 갱신하는 스케줄러

```kotlin
@Scheduled(cron = "0 0 0 * * *")
fun generateProductRank() {
    productRankService.refreshRank(LocalDate.now())
}
```

```kotlin
companion object {
    private const val DST_KEY_PATTERN = "product:rank:%s:%s"
    private const val SRC_KEY_PATTERN = "product:sales:%s"
    private val TTL = Duration.ofHours(25)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
}

fun refreshRank(baseDate: LocalDate) {
    val threeDaysRankDstKey = DST_KEY_PATTERN.format("3d", dateFormatter.format(baseDate))
    val sevenDaysRankDstKey = DST_KEY_PATTERN.format("1w", dateFormatter.format(baseDate))

    val threeDaysRankSrcKeys = (1L .. 3L).map {
        SRC_KEY_PATTERN.format(
            baseDate.minusDays(it).format(dateFormatter)
        )
    }
    val sevenDaysRankSrcKeys = (1L .. 7L).map {
        SRC_KEY_PATTERN.format(
            baseDate.minusDays(it).format(dateFormatter)
        )
    }

    redisSortedSetRepository.unionAndStore(threeDaysRankSrcKeys, threeDaysRankDstKey, TTL)
    redisSortedSetRepository.unionAndStore(sevenDaysRankSrcKeys, sevenDaysRankDstKey, TTL)
}
```

- 매일 자정에 실행되는 스케줄러로, 날짜별 상품 랭킹(`product:sales:{yyyyMMdd}`)들을 집계
- `ZUNIONSTORE` 작업을 수행해서 각각 `product:rank:3d:{yyyyMMdd}`, `product:rank:1w:{yyyyMMdd}` 키로 저장
- 캐시 안정성 확보를 위해 25시간의 TTL 적용

### 인기 상품 조회 함수

```kotlin
companion object {
    private const val DST_KEY_PATTERN = "product:rank:%s:%s"
    private const val SRC_KEY_PATTERN = "product:sales:%s"
}

fun getProductRanks(since: LocalDate, periodKey: String): List<ProductInfo.Rank> {
    val today = LocalDate.now()
    val redisKey = DST_KEY_PATTERN.format(periodKey, today.format(dateFormatter))
    val betweenDays = ChronoUnit.DAYS.between(since, today)

    return if (redisRepository.exists(redisKey)) {
        log.info("✅ [Cache Hit]")
        getProductRanksFromRedis(redisKey)

    } else if (betweenDays <= 7) {
        log.info("⚠️ [Cache Miss] → Redis ZSet 랭킹 생성")
        cacheProductRankByUnion(since, today, redisKey, periodKey)
        getProductRanksFromRedis(redisKey)
    } else {
        log.info("⚠️ [Cache Miss] → 기간 범위를 초과하여 DB 조회")
        val ttl = ttlForPeriodKey(periodKey)
        getProductRanksFromDatabase(since, redisKey, ttl)
    }
}
```

- 요청을 통해 받은 기간 조건(`since`, `periodKey`) 값에 따라 적절하게 캐시 조회 혹은 DB 조회 작업
- 요청에 알맞는 키(`product:rank:{periodKey}:{yyyyMMdd}`)가 존재하는 경우
  - Cache Hit로 판단하고 해당 ZSet 정보 반환
- 캐시가 존재하지 않는 경우
  - 7일 이내에 대한 요청이면 `ZUNIONSTORE` 작업 진행 후 1시간 캐싱
  - 7일 범위 밖에 대한 요청이면 DB 조회 후 1시간 캐싱

<br>

## 4. 추가로 고려해볼 만한 사항들

이번 과제에서는 주어진 범위(일간, 3일, 7일) 단위의 인기 상품 랭킹을 구현하는 데에 초점을 맞추었습니다.  
하지만 서비스의 규모가 확장되거나, 비즈니스 요구사항이 달라짐에 따라 다음과 같은 측면들을 함께 고려해볼 수 있을 것입니다.

### Sliding Window 방식의 랭킹 집계

현재 3일 및 7일에 대한 랭킹 집계는 매일 자정에 이루어지고 있습니다.  
그렇기 때문에 집계가 변경되는 시점의 랭킹 변화가 사용가 입장에서 갑작스럽게 느껴질 수 있다는 우려점이 존재합니다.  
Sliding Window 방식을 활용한다면, 날짜가 아닌 시간 단위로 갱신하는 랭킹 시스템을 구현할 수 있기 때문에 UX 측면에서 좀 더 유리한 점을 가질 수 있습니다.  
다만 그만큼 Redis에 적재해야 하는 데이터가 많아질 것이고, 구현 복잡도도 올라갈 것입니다.  
따라서 이는 실제 비즈니스 상에서 시간대별 사용자 수 등을 긴밀하게 파악하고 있으며, 이에 걸맞는 UX를 제공하고 싶다는 판단이 있었을 때에 도입하면 좋을 것으로 예상됩니다.

### 월간 단위의 랭킹 확장

저희 서비스는 신규 E-커머스 서비스이고, 오래된 상품 결제에 대한 데이터가 많지 않기 때문에 월간 랭킹을 따로 구현하지 않았습니다.  
하지만 향후 서비스 규모가 커지고 주문 데이터가 풍부해진다면 월간 랭킹 데이터도 충분히 집계할 수 있을 것이고, **"이번 달 베스트 상품"** 을 강조하는 마케팅 방안을 활용해볼 수 있게 될 것입니다.  
그러한 요구사항이 주어질 경우에는 Redis 보다 DB 기반의 배치 집계, 혹은 Elasticsearch 등의 별도의 분석 도구를 활용하는 것이 유리할 것으로 판단됩니다.

### 가중치 추가

현재 구현된 상품 랭킹 시스템은 **판매 수량**만을 기준으로 삼고 있습니다.  
앞으로 서비스가 확장되어 감에 따라 더욱 다양한 데이터를 집계할 수 있게 된다면, '좋아요 수', '상품 조회 수', '특정 카테고리 내 랭킹' 등의 다양한 **가중치 조건**들과 함께 랭킹 시스템을 더욱 고도화해볼 수 있을 것입니다.
