# 쿠폰 발급 관련 가상 장애 대응 문서

## 장애 상황 인지

6월 5일 16시 30분경, 배포된 애플리케이션의 로그를 확인하던 중에 문득 아래와 같은 로그를 확인하게 되었습니다.

```sh
2025-06-05 14:32:47.812 ERROR 1 --- [nio-8080-exec-3] o.s.d.r.c.l.LettuceExceptionConverter    : Redis command execution failed

org.springframework.dao.QueryTimeoutException: Redis command timed out; nested exception is io.lettuce.core.RedisCommandTimeoutException: Command timed out
	at org.springframework.data.redis.connection.lettuce.LettuceExceptionConverter.convert(LettuceExceptionConverter.java:66)
	at org.springframework.data.redis.connection.lettuce.LettuceExceptionConverter.convert(LettuceExceptionConverter.java:41)
	at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:228)
	...

Caused by: io.lettuce.core.RedisCommandTimeoutException: Command timed out
```

해당 로그는 발견 시점으로부터 무려 **2시간 전에** 발생했던 것이며, 발생 시점 이후로 쿠폰 발급 API에 대한 응답이 급격히 느려지고 대다수의 요청이 실패하는 현상이 관측되었습니다.  
이는 사용자 경험에 치명적인 영향을 주는 장애였으므로, 빠른 원인 분석과 대응이 필요한 상황이었습니다.

<br>

## 장애 현상 요약

주요 서비스 기능들을 로그를 통해 확인해본 결과, Redis를 활용하고 있던 아래의 핵심 기능들에 영향을 미치고 있었다는 사실이 파악되었습니다.

| 기능             | 장애 현상 설명                                                   | 장애 영향도      |
|------------------|------------------------------------------------------------------|------------------|
| 쿠폰 발급 기능    | Redis 기반의 쿠폰 발급 로직 지연 및 실패 | 🔴 매우 높음      |
| 인기 상품 조회    | Redis 기반 조회 실패 시 DB로 fallback 조회하도록 구성되어 기능은 유지됨   | 🟡 낮음 (성능 저하) |

피상적으로는 Redis 서버에 모종의 이유로 심각한 지연 현상이 발생한 것으로 파악되었습니다.  
문제를 빠르게 해결하는데에 초점을 둔다면 단순히 Redis 서버를 재실행하는 것이 해답일 수도 있겠으나, 같은 현상이 반복될 가능성이 높기 때문에 원인을 정확히 진단하고 해결하는 과정이 필요하다고 판단했습니다.

<br>

## 장애 원인 분석

### 의심 포인트: GPT-5 기반 쿠폰 자동 생성 시스템

![virtual-incident-response-1](./virtual-incident-response/gpt-5.webp)

최근 신규 고객 유입과 서비스 확장을 위한 실험적 시도로, 획기적인 시스템을 도입한 바 있습니다.  
바로, 차세대 AI 모델인 **GPT-5 베타 버전**을 활용해 **자동 쿠폰 생성 시스템**을 구축한 것입니다.  
이 시스템은 쿠폰 이름과 할인율, 기간 등을 조합해 마치 창의적인 마케터처럼 유행에 뒤쳐지지 않는 센세이셔널한 쿠폰들을 자동으로 만들어내는 기능을 수행했습니다.

하지만 해당 시스템에 **생성 임계값**을 제대로 설정하지 않은 탓에, GPT-5는 10초마다 쿠폰을 생성했고 하루에 8,640개의 쿠폰을 생성하기에 이르렀습니다.  
따라서 이 시스템이 Redis 메모리에 부하를 증가시켰고, 이에 따라 쿠폰 발급 기능에 심각한 영향을 미쳤다고 예측하게 되었습니다.

### Redis OOM (Out of Memory) 현상 파악

Redis의 상황을 최우선적으로 확인해야겠다는 판단 하에 Redis 서버의 로그를 먼저 확인해봤으며, 아래와 같은 메시지가 지속적으로 출력되고 있는 것을 확인할 수 있었습니다.

```sh
OOM command not allowed when used memory > 'maxmemory'.
```

이는 Redis가 내부적으로 메모리 한계에 도달하면서, 쓰기 관련 명령을 거부하는 상황으로 해석됩니다.  
특히 TTL이 너무 길거나, TTL 자체가 설정되지 않은 key들이 과도하게 누적될 경우 이러한 문제가 발생할 수 있습니다.

조금 더 나아가서, Redis의 메모리 관련 설정 값을 확인해보기 위해 다음 명령어를 실행했습니다.

```sh
redis-cli INFO memory
```

확인 결과 아래와 같은 설정값이 문제의 소지가 될 수 있다는 점을 발견했습니다.

```sh
maxmemory:0
maxmemory_policy:noeviction
```

`maxmemory:0` 설정은 Redis가 사용할 수 있는 메모리의 상한을 두지 않은 상태를 의미합니다.  
또한 `maxmemory_policy:noeviction` 설정은 메모리가 가득찬 상황에서도 키들이 자동으로 제거되지 않음을 뜻합니다.  
따라서 메모리 부족 현상이 발생하면 새로운 데이터를 쓰지 못하게 되고, 이에 따라 심각한 지연이나 에러로 이어질 수 있습니다.

<br>

## 장애 대응 과정

원인을 모두 파악한 뒤, 16시 45분경부터 장애 대응 작업이 진행되었습니다.

### GPT-5 기반 쿠폰 자동 생성 시스템 비활성화 (30분 이내)

장애 발견 시점에도 쿠폰 자동 생성 시스템이 작동 중인 상태였기 때문에 해당 서비스를 일시적으로 중단했습니다.  
이어서, 이미 생성된 수많은 쿠폰들에 대해서는 발급 받은 사용자가 존재하지 않는다는 조건 하에 삭제 처리를 진행했습니다.

### Redis 메모리 제한 설정 (1시간 이내)

현재 우리 서비스는 Docker 컨테이너를 기반으로 Redis 서비스를 운용 중인 상태입니다.  
그리고 기존에 사용하던 Redis 구동 관련 `docker-compose` 스크립트는 아래와 같았습니다.

**AS-IS**

```yml
version: '3'
services:
  redis:
    image: redis:latest
    container_name: redis
    ports:
      - "6379:6379"
    command: redis-server --requirepass root
```

사실 기존 스크립트만으로도 1만 건에 달하는 데이터를 안정적으로 처리할 수 있었지만, 메모리 부하가 일정 수준을 초과하면 시스템이 이를 감당하지 못할 가능성이 있었습니다.  
따라서 아래와 같이 메모리 관련 설정을 추가했습니다.

**TO-BE**

```yml
version: '3'
services:
  redis:
    image: redis:latest
    container_name: redis
    ports:
      - "6379:6379"
    command: >
      redis-server
      --requirepass root
      --maxmemory 4gb
      --maxmemory-policy volatile-lru
    volumes:
      - ./scripts/lua:/scripts
    networks:
      - hhplus-net
    deploy:
      resources:
        limits:
          memory: 4g
        reservations:
          memory: 2g
```

이러한 설정으로 Redis 컨테이너는 최대 4GB의 메모리를 사용하게 되었으며, 만약 메모리가 가득 찼을 경우에는 TTL이 있는 키 중에서 가장 오래 사용되지 않은 키를 삭제하게 되었습니다.

하지만 이는 어디까지나 긴급 조치를 위한 수단이었을 뿐이며, 아직 미비한 부분들이 많습니다.  
특히 `maxmemory-policy volatile-lru` 설정은 의도치 않게 사용자의 보유 쿠폰을 삭제해버릴 수 있다는 우려점이 있기 때문에, 후속 조치에서 더욱 더 보완되어야 할 것입니다.

<br>

## 향후 후속 조치

이번 장애를 겪으며 우리 팀은 Redis 운용에 미숙하다는 점을 깨달았습니다.  
현재 서비스에 Redis를 활용하는 기능들이 많아지고 있는 만큼, Redis와 관련된 여러 가지 후속 작업들을 검토해야 할 것으로 보입니다.

### Redis 고가용성 (HA) 환경 구성

현재는 단일 Redis 서버를 활용하고 있으므로, 이번에 다룬 장애 사태 발생 시에 매우 취약한 상태입니다.  
따라서 Sentinel 기능을 활용한 Redis Cluster를 구성한다면, 고가용성을 확보해서 장애를 방지할 수 있을 것입니다.  
하지만 현재로서는 팀 내 Redis 운용 능력이 낮은 상태이므로, 후순위로 고려할 대상이라고 생각합니다.

### 관리형 Redis 서비스 (AWS ElastiCache) 도입

현재 Redis 서비스는 앞서 언급했듯이 Docker를 활용해서 다른 외부 의존 서비스들과 함께 배포되고 있는 상황입니다.  
따라서 다소 운영 부담이 있는 상태이고, 이번과 같은 장애 사태가 발생할 때마다 메모리 등의 값을 직접 설정해줘야 합니다.  
이런 상황에서 AWS ElastiCache 등의 Redis 관리형 서비스를 도입하게 된다면 운영 및 관리 비용을 확실하게 줄여줄 수 있을 것입니다.  
또한 원한다면 HA 및 Sentinel을 활용하도록 선택할 수 있기 때문에 가용성 측면에서도 유리한 선택지입니다.  
하지만 인프라 비용 지출이 늘어나게 되므로 신중하게 검토된 이후에 도입이 진행되어야 할 것입니다.

### 실시간 모니터링 및 알림 시스템 도입

Redis와는 별개로, 이번 장애 사태에는 실제 서비스 장애 이후 무려 2시간 이후에 장애 상황을 파악하게 되었다는 치명적인 문제점이 존재합니다.  
심지어 개발자가 로그를 확인해보지 않았다면 더 늦게 조치가 이루어졌을 수도 있었습니다.

따라서 실시간 모니터링 도구 및 알림 시스템의 도입이 무엇보다도 시급한 상태입니다.  
일단 **Prometheus**, **Grafana** 기반의 모니터링 환경 구축이 가장 먼저 진행되어야 할 후속 작업이라고 생각합니다.  
이 작업이 이루어진다면 개발자가 지속적으로 모니터링 도구를 확인하며 시스템의 문제점을 지속적으로 팔로업하는 것이 가능해질 것입니다.  
추가적으로, 개발자가 다른 업무 중에도 장애 상황을 빠르게 파악할 수 있도록 Slack 혹은 이메일을 활용한 알림 시스템도 조속히 개발되어야 할 것입니다.  
장애 상황을 보다 빠르게 파악할 수 있다면, 서비스 중단 시간을 최소화하고 피해 확산을 보다 효과적으로 막을 수 있을 것입니다.
