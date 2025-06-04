# 부하 테스트 설계 문서

## 서론

현재 우리 E-커머스 서비스는 MVP로서 갖춰야 할 핵심적인 기능들에 대한 구현을 끝마친 상태입니다.  
그리고 이제는 배포 단계에 앞서, 서비스의 안정성 및 성능을 검증할 차례입니다.

개발 단계에서도 동시성 제어 로직, 이벤트 기반의 분산 트랜잭션 등을 적용하여 안정성 및 가용성을 높여 왔습니다.  
하지만 실제로 서비스가 배포된 환경에서 단기간에 많은 트래픽을 받았을 때 서비스가 어떤 반응을 보일지는 여전히 예측이 불가능합니다.

이에 따라 부하 테스트의 필요성이 대두되었으며, 본 보고서에서는 어떤 방향으로 서비스에 대한 부하 테스트를 진행할 것인지에 대해 기술할 예정입니다.

<br>

## 부하 테스트 목표 및 설계 방향

### 테스트 대상 선정 및 이유

이번 부하 테스트에서는 다음 2가지의 핵심 기능들을 중점적으로 테스트할 계획입니다.

- **선착순 쿠폰 발급 API**

선착순 쿠폰 발급 이벤트를 진행할 경우, 수천 명이 동시에 쿠폰 발급을 요청할 수 있습니다.  
따라서 우리 서비스의 쿠폰 발급 API가 **단기간의 스파이크 트래픽**을 견뎌낼 수 있을지에 대한 검증이 필요합니다.

- **주문 및 결제 API**

주문 및 결제 API는 우리 서비스의 핵심적인 기능들이라고 할 수 있으며, 안정적인 정합성이 무엇보다도 중요하게 여겨지는 포인트입니다.  
그러므로 지속적인 트래픽 부하 상황에서도 안정적으로 기능을 수행하는지 검증되어야 합니다.

### SLA / SLO / SLI 정의

**SLA(Service Level Agreement)**

현재 우리 서비스는 아직 MVP 단계입니다.  
따라서 고객에게 **SLA**를 법적으로 확실하게 보장할 수 있도록 해주는 운영 지표 및 실측 데이터가 미비한 상태입니다.  
그러므로 현 단계에서 진행할 부하 테스트에서는 내부적으로 설정한 목표치인 **SLO**를 기준점으로 삼고, 실제 테스트를 통해 **SLI**가 어떻게 계측되는지를 중점적으로 살펴볼 계획입니다.  
본 부하 테스트 단계 이후에 명시적인 **SLA** 도출을 위한 경영진과의 협의가 진행될 것으로 예상됩니다.

**SLO(Service Level Objective)**

본 테스트에서는 시스템의 핵심적인 기능들이 얼마나 안정적이고 빠르게 응답하는지를 검증하고자 합니다.  
이를 위해 각 테스트 대상에 대해 다음과 같은 **SLO**를 설정했습니다.

| 테스트 대상           | Success Rate 기준                            | Latency 기준           |
|------------------------|----------------------------------------------|-------------------------|
| 선착순 쿠폰 발급 (Peak Test) | 99% 이상<br>(HTTP 201, 409 응답을 정상으로 간주) | P95 500ms 이하         |
| 주문 및 결제 (Load Test)     | 99.9% 이상                                 | P90 1초 이하            |

선착순 쿠폰 발급 기능은 순간적인 트래픽 부하에도 빠른 처리율을 가진 API를 제공할 수 있도록 하는 데에 초점을 뒀습니다.  
따라서 성공률보다도 **높은 처리율에 따른 낮은 지연 시간**을 보이는지를 중점적으로 살펴보고자 합니다.

그리고 주문 및 결제 API는 그 무엇보다도 **정합성**이 중요하다고 판단했습니다.  
따라서 RPS는 1초 정도로 다소 낮을지 몰라도, 성공률을 99.9% 수준으로 최대한 보장하는 쪽으로 방향을 잡았습니다.

**SLI(Service Level Indicator)**

이번 부하 테스트에서는 응답 성공률(HTTP 201, 409) 및 응답 지연 시간(P95, P99)을 주요 **SLI** 지표로 활용할 예정입니다.  
**SLI** 지표들은 **SLO** 목표치와 비교해서 서비스의 안정성 및 성능을 판단할 수 있는 합리적인 근거로서 제공될 예정이며, 추후 **SLA**를 도출해내는 데에도 핵심적인 역할을 수행할 것입니다.

실제 SLI 수치 결과들은 부하 테스트 결과를 진행한 이후 자세하게 다룰 예정입니다.

<br>

## 부하 테스트 세부사항

### Spring 애플리케이션을 Docker로 실행하기

본격적으로 부하 테스트를 진행하기에 앞서, 기존에 로컬 환경에서만 실행해보던 개발 환경을 Docker 환경으로도 구성해보기로 했습니다.  
본 작업은 배포 환경에서의 CPU, 메모리 제약에 따른 실제 부하 요청 처리 수행 능력을 최대한 같게 맞추기 위해서 진행되었습니다.

**Dockerfile**

```sh
FROM amazoncorretto:17.0.15
COPY build/libs/app.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

`app.jar` 파일로 빌드된 Spring 애플리케이션을 기존에 사용하던 Java 17 버전의 이미지와 함께 실행합니다.  
`docker run` 명령어를 실행할 때, 추가적인 Java 실행 옵션을 부여할 수 있도록 구성했습니다.

**Docker 실행 스크립트**

```sh
#!/bin/bash

IMAGE_NAME="hhplus-e-commerce-image"
CONTAINER_NAME="hhplus-e-commerce"
NETWORK_NAME="hhplus-e-commerce_hhplus-net"

docker build -t $IMAGE_NAME .
docker rm -f $CONTAINER_NAME 2>/dev/null || true
docker run --rm -p 8080:8080 \
  --name $CONTAINER_NAME \
  --network $NETWORK_NAME \
  --cpus="4.0" \
  --memory="10g" \
  -e JAVA_OPTS="-Xms1024m -Xmx8192m" \
  -e SPRING_PROFILES_ACTIVE=dev \
  $IMAGE_NAME
```

Dockerfile을 참조하여 컨테이너를 빌드하고 실행합니다.

`--cpus="4.0"` 및 `--memory="10g"` 설정은 테스트 시나리오에서 사용할 부하량을 기준으로 역산해서 산정한 값입니다.  
테스트 대상 중, 선착순 쿠폰 발급 기능에 대한 Peak Test는 순간적인 높은 트래픽을 예상해서 VU를 1,500 단위로 설정했습니다.  
이러한 부하 테스트 수준을 감당할 수 있도록, CPU 및 메모리 설정을 다소 높게 정해두었습니다.

CPU 설정은 주로 Spring Boot 초기 구동 과정에서 핵심적으로 쓰이지만, 가동 중 메모리가 부족해질 때 사용률이 높아질 수 있으므로 이에 알맞은 넉넉하게 **4 코어**로 설정했습니다.  
그리고 메모리 설정은 다소 추상적인 값이긴 합니다만, 1,500 VU가 초당 15,000개의 요청을 전송한다고 가정했고, 각 요청은 100KB의 메모리를 사용한다고 했을 때, 총 1.5GB의 메모리를 지속적으로 사용한다고 예상했습니다.  
여기에 실제 GC 버퍼 및 각종 Spring 객체들의 메모리 크기를 고려해 4배 정도의 여유 공간 확보가 필요하다고 생각해서, Java에서는 최대 8GB의 메모리값 설정을, Docker 컨테이너에서는 더욱 여유 공간을 두어서 10GB의 메모리값 설정을 적용했습니다.

실제로 AWS 환경에 애플리케이션을 배포했을 경우에는 EC2 [`t3.xlarge`](https://aws.amazon.com/ko/ec2/instance-types/t3/) 정도의 스펙을 사용한다고 가정할 수 있습니다.

### 부하 테스트를 위한 데이터

```sql
INSERT INTO customer (id, username, created_at, updated_at)
VALUES
  (1001, 'user_1001', NOW(), NOW()),
  (1002, 'user_1002', NOW(), NOW()),
  (1003, 'user_1003', NOW(), NOW()),
  ...

INSERT INTO balance (id, customer_id, amount, created_at, updated_at)
VALUES
  (1001, 1001, 50000000, NOW(), NOW()),
  (1002, 1002, 50000000, NOW(), NOW()),
  (1003, 1003, 50000000, NOW(), NOW()),
  ...
```

- 최대 VU 1,500까지 테스트하므로, 그보다 좀 더 넉넉하게 2,000개의 고객 데이터 생성
  - ID: 1,001 ~ 3,000
- 장기간 부하 테스트에도 무리가 없도록 5,000만의 넉넉한 잔액 부여

```sql
INSERT INTO coupon (
  id, name, discount_type, discount_amount, current_quantity, total_quantity,
  started_at, expired_at, created_at, updated_at
)
VALUES (
  4001, '이벤트 한정 1000원 할인', 'FIXED', 1000, 1000, 1000,
  NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), NOW(), NOW()
)
```

- 하나의 선착순 쿠폰 발급 이벤트가 있다고 가정해서 1,000장의 재고가 있는 쿠폰 하나 데이터 생성
  - ID: 4,001

```sql
INSERT INTO product (id, name, base_price, created_at, updated_at)
VALUES
  (5001, '티셔츠', 10000, NOW(), NOW()),
  (5002, '후드티', 15000, NOW(), NOW()),
  (5003, '청바지', 20000, NOW(), NOW()),
  (5004, '운동화', 30000, NOW(), NOW()),
  (5005, '점퍼', 25000, NOW(), NOW()),
  (5006, '셔츠', 12000, NOW(), NOW()),
  (5007, '슬랙스', 18000, NOW(), NOW()),
  (5008, '모자', 8000, NOW(), NOW()),
  (5009, '스니커즈', 22000, NOW(), NOW()),
  (5010, '가방', 40000, NOW(), NOW()),
  (5011, '슬랙스_1', 30000, NOW(), NOW()),
  (5012, '모자_2', 18000, NOW(), NOW()),
  (5013, '운동화_3', 25000, NOW(), NOW()),
  (5014, '청바지_4', 15000, NOW(), NOW()),
  (5015, '모자_5', 8000, NOW(), NOW()),
  (5016, '점퍼_6', 20000, NOW(), NOW()),
  (5017, '셔츠_7', 18000, NOW(), NOW()),
  (5018, '가방_8', 18000, NOW(), NOW()),
  (5019, '후드티_9', 20000, NOW(), NOW()),
  (5020, '모자_10', 22000, NOW(), NOW());

INSERT INTO product_option (id, product_id, option_name, extra_price, created_at, updated_at)
VALUES
  (6001, 5001, 'S', 0, NOW(), NOW()), (6002, 5001, 'M', 1000, NOW(), NOW()), (6003, 5001, 'L', 2000, NOW(), NOW()),
  (6004, 5002, 'S', 0, NOW(), NOW()), (6005, 5002, 'M', 1000, NOW(), NOW()), (6006, 5002, 'L', 2000, NOW(), NOW()),
  (6007, 5003, 'S', 0, NOW(), NOW()), (6008, 5003, 'M', 1000, NOW(), NOW()), (6009, 5003, 'L', 2000, NOW(), NOW()),
  (6010, 5004, 'S', 0, NOW(), NOW()), (6011, 5004, 'M', 1000, NOW(), NOW()), (6012, 5004, 'L', 2000, NOW(), NOW()),
  (6013, 5005, 'S', 0, NOW(), NOW()), (6014, 5005, 'M', 1000, NOW(), NOW()), (6015, 5005, 'L', 2000, NOW(), NOW()),
  (6016, 5006, 'S', 0, NOW(), NOW()), (6017, 5006, 'M', 1000, NOW(), NOW()), (6018, 5006, 'L', 2000, NOW(), NOW()),
  (6019, 5007, 'S', 0, NOW(), NOW()), (6020, 5007, 'M', 1000, NOW(), NOW()), (6021, 5007, 'L', 2000, NOW(), NOW()),
  (6022, 5008, 'S', 0, NOW(), NOW()), (6023, 5008, 'M', 1000, NOW(), NOW()), (6024, 5008, 'L', 2000, NOW(), NOW()),
  (6025, 5009, 'S', 0, NOW(), NOW()), (6026, 5009, 'M', 1000, NOW(), NOW()), (6027, 5009, 'L', 2000, NOW(), NOW()),
  (6028, 5010, 'S', 0, NOW(), NOW()), (6029, 5010, 'M', 1000, NOW(), NOW()), (6030, 5010, 'L', 2000, NOW(), NOW()),
  (6031, 5011, 'S', 0, NOW(), NOW()), (6032, 5011, 'M', 1000, NOW(), NOW()), (6033, 5011, 'L', 2000, NOW(), NOW()),
  (6034, 5012, 'S', 0, NOW(), NOW()), (6035, 5012, 'M', 1000, NOW(), NOW()), (6036, 5012, 'L', 2000, NOW(), NOW()),
  (6037, 5013, 'S', 0, NOW(), NOW()), (6038, 5013, 'M', 1000, NOW(), NOW()), (6039, 5013, 'L', 2000, NOW(), NOW()),
  (6040, 5014, 'S', 0, NOW(), NOW()), (6041, 5014, 'M', 1000, NOW(), NOW()), (6042, 5014, 'L', 2000, NOW(), NOW()),
  (6043, 5015, 'S', 0, NOW(), NOW()), (6044, 5015, 'M', 1000, NOW(), NOW()), (6045, 5015, 'L', 2000, NOW(), NOW()),
  (6046, 5016, 'S', 0, NOW(), NOW()), (6047, 5016, 'M', 1000, NOW(), NOW()), (6048, 5016, 'L', 2000, NOW(), NOW()),
  (6049, 5017, 'S', 0, NOW(), NOW()), (6050, 5017, 'M', 1000, NOW(), NOW()), (6051, 5017, 'L', 2000, NOW(), NOW()),
  (6052, 5018, 'S', 0, NOW(), NOW()), (6053, 5018, 'M', 1000, NOW(), NOW()), (6054, 5018, 'L', 2000, NOW(), NOW()),
  (6055, 5019, 'S', 0, NOW(), NOW()), (6056, 5019, 'M', 1000, NOW(), NOW()), (6057, 5019, 'L', 2000, NOW(), NOW()),
  (6058, 5020, 'S', 0, NOW(), NOW()), (6059, 5020, 'M', 1000, NOW(), NOW()), (6060, 5020, 'L', 2000, NOW(), NOW());

INSERT INTO stock (id, product_option_id, quantity, updated_at)
VALUES
  (7001, 6001, 100000, NOW()), (7002, 6002, 100000, NOW()), (7003, 6003, 100000, NOW()),
  (7004, 6004, 100000, NOW()), (7005, 6005, 100000, NOW()), (7006, 6006, 100000, NOW()),
  (7007, 6007, 100000, NOW()), (7008, 6008, 100000, NOW()), (7009, 6009, 100000, NOW()),
  (7010, 6010, 100000, NOW()), (7011, 6011, 100000, NOW()), (7012, 6012, 100000, NOW()),
  (7013, 6013, 100000, NOW()), (7014, 6014, 100000, NOW()), (7015, 6015, 100000, NOW()),
  (7016, 6016, 100000, NOW()), (7017, 6017, 100000, NOW()), (7018, 6018, 100000, NOW()),
  (7019, 6019, 100000, NOW()), (7020, 6020, 100000, NOW()), (7021, 6021, 100000, NOW()),
  (7022, 6022, 100000, NOW()), (7023, 6023, 100000, NOW()), (7024, 6024, 100000, NOW()),
  (7025, 6025, 100000, NOW()), (7026, 6026, 100000, NOW()), (7027, 6027, 100000, NOW()),
  (7028, 6028, 100000, NOW()), (7029, 6029, 100000, NOW()), (7030, 6030, 100000, NOW()),
  (7031, 6031, 100000, NOW()), (7032, 6032, 100000, NOW()), (7033, 6033, 100000, NOW()),
  (7034, 6034, 100000, NOW()), (7035, 6035, 100000, NOW()), (7036, 6036, 100000, NOW()),
  (7037, 6037, 100000, NOW()), (7038, 6038, 100000, NOW()), (7039, 6039, 100000, NOW()),
  (7040, 6040, 100000, NOW()), (7041, 6041, 100000, NOW()), (7042, 6042, 100000, NOW()),
  (7043, 6043, 100000, NOW()), (7044, 6044, 100000, NOW()), (7045, 6045, 100000, NOW()),
  (7046, 6046, 100000, NOW()), (7047, 6047, 100000, NOW()), (7048, 6048, 100000, NOW()),
  (7049, 6049, 100000, NOW()), (7050, 6050, 100000, NOW()), (7051, 6051, 100000, NOW()),
  (7052, 6052, 100000, NOW()), (7053, 6053, 100000, NOW()), (7054, 6054, 100000, NOW()),
  (7055, 6055, 100000, NOW()), (7056, 6056, 100000, NOW()), (7057, 6057, 100000, NOW()),
  (7058, 6058, 100000, NOW()), (7059, 6059, 100000, NOW()), (7060, 6060, 100000, NOW());
```

- 더미 상품 데이터 20개 및 각 옵션 3개씩 생성
  - 상품 ID: 5,001 ~ 5,020
  - 옵션 ID: 6,001 ~ 6,060
  - 재고 ID: 7,001 ~ 7,060
- 주문 결제 부하 테스트에도 수량이 부족하지 않도록 재고 수량을 10만으로 설정

### 쿠폰 발급 부하 테스트 k6 스크립트

```js
const COUPON_ID = 4001;
const BASE_URL = 'http://localhost:8080';

export const createdCount = new Counter('coupon_201_count');
export const conflictCount = new Counter('coupon_409_count');
export const otherCount = new Counter('coupon_other_count');

export const createdRate = new Rate('coupon_201_rate');
export const conflictRate = new Rate('coupon_409_rate');
export const otherRate = new Rate('coupon_other_rate');

export const options = {
  stages: [
    { duration: '1s', target: 10 },
    { duration: '2s', target: 500 },
    { duration: '3s', target: 1500 },
    { duration: '3s', target: 1000 },
    { duration: '1s', target: 10 }
  ],
  thresholds: {
    http_req_duration: [
      {
        threshold: 'p(95)<500',
        abortOnFail: false
      }
    ]
  }
};

export default function () {
  const customerId = 1001 + Math.floor(Math.random() * 2000);

  const res = http.post(`${BASE_URL}/coupons/issue`, JSON.stringify({
    couponId: COUPON_ID,
    customerId
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  const status = res.status;

  createdRate.add(status === 201);
  conflictRate.add(status === 409);
  otherRate.add(status !== 201 && status !== 409);

  if (status === 201) {
    createdCount.add(1);
  } else if (status === 409) {
    conflictCount.add(1);
  } else {
    otherCount.add(1);
    console.warn(`🚨 Unexpected response: status=${status}, body=${res.body}`);
  }

  check(res, {
    'is 201 or 409': (r) => r.status === 201 || r.status === 409
  });
}
```

- 총 10초 동안 최대 1,500 VU의 요청을 테스트
- 미리 생성해둔 1,001 ~ 3,000 까지의 `customerId` 대상으로 4,001 ID를 가진 쿠폰 발급
- 쿠폰 중복 발급 및 수량 부족으로 인한 409 에러까지 정상 응답으로 간주
- HTTP 201, 409 응답 성공률 및 P95 500ms 체크

### 주문 및 결제 부하 테스트 k6 스크립트

```js
const BASE_URL = 'http://localhost:8080';

export const createdCount = new Counter('payment_201_count');
export const conflictCount = new Counter('payment_409_count');
export const otherCount = new Counter('payment_other_count');

export const createdRate = new Rate('payment_201_rate');
export const conflictRate = new Rate('payment_409_rate');
export const otherRate = new Rate('payment_other_rate');

export let options = {
  stages: [
    { duration: '1m', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 10 },
  ],
  thresholds: {
    http_req_duration: [
      {
        threshold: 'p(90)<1000',
        abortOnFail: false
      }
    ],
    http_req_failed: [
      {
        threshold: 'rate<0.001',
        abortOnFail: false
      }
    ]
  }
};

export default function () {
  const customerId = 1001 + ((__VU - 1) * 100 + __ITER) % 2000;

  const productId1 = 5001 + Math.floor(Math.random() * 20);
  const productId2 = 5001 + Math.floor(Math.random() * 20);

  const optionS1 = 3 * (productId1 - 5001) + 6001;
  const optionM2 = 3 * (productId2 - 5001) + 6002;

  // Step 1. 주문 생성
  const orderPayload = JSON.stringify({
    customerId,
    items: [
      {
        productId: productId1,
        productOptionId: optionS1,
        quantity: 1
      },
      {
        productId: productId2,
        productOptionId: optionM2,
        quantity: 1
      }
    ]
  });

  const orderRes = http.post(`${BASE_URL}/orders`, orderPayload, {
    headers: { 'Content-Type': 'application/json' }
  });

  check(orderRes, {
    'order created': (r) => r.status === 201,
  });

  if (orderRes.status !== 201) {
    console.warn(`🚨 Order creation failed: status=${orderRes.status}, body=${orderRes.body}`);
    return;
  }

  const orderId = orderRes.json().orderId;
  if (!orderId) {
    console.warn('orderId not found in response');
    return;
  }

  // Step 2. 결제 요청
  const paymentPayload = JSON.stringify({
    orderId
  });

  const paymentRes = http.post(`${BASE_URL}/payments`, paymentPayload, {
    headers: { 'Content-Type': 'application/json' }
  });

  const status = paymentRes.status;

  createdRate.add(status === 201);
  conflictRate.add(status === 409);
  otherRate.add(status !== 201 && status !== 409);

  if (status === 201) createdCount.add(1);
  else if (status === 409) {
    console.warn(`🚨 Payment failed: body=${paymentRes.body}`);
    conflictCount.add(1);
  }
  else otherCount.add(1);

  check(paymentRes, {
    'payment success': (r) => r.status === 201
  });
}
```

- 총 5분 동안 최대 100 VU의 요청이 꾸준히 지속되는 상황에 대한 테스트
- 주문 생성 API 이후 결제 API까지 진행되는 짧은 시나리오 테스트 유형
- 1,001 ~ 3,000 까지의 고객이 5,001 ~ 5,020 까지의 상품 및 6,001 ~ 6,020까지의 상품 옵션을 대상으로 주문 생성 및 결제
- 성공률 99.9% 및 P90 1s 체크

### 시각화를 위한 도구 설정 (docker-compose)

```yml
version: '3'
services:
  influxdb:
    image: influxdb:1.8
    container_name: influxdb
    ports:
      - "8086:8086"
    environment:
      - INFLUXDB_DB=k6
      - INFLUXDB_HTTP_AUTH_ENABLED=true
      - INFLUXDB_ADMIN_USER=admin
      - INFLUXDB_ADMIN_PASSWORD=root
    volumes:
      - influxdb-data:/var/lib/influxdb
    networks:
      - hhplus-net

  grafana:
    image: grafana/grafana
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=root
    volumes:
      - grafana-data:/var/lib/grafana
    networks:
      - hhplus-net
```

k6 외에도 grafana, influxdb를 Docker와 함께 활용해서 시각화 방안을 마련했습니다.

![grafana-influxdb-setup](./load-test/grafana-influxdb-setup.png)

<br>

## 부하 테스트 수행 결과 분석

### 선착순 쿠폰 발급 Peak Test

**k6 실행 결과**

```sh
  █ THRESHOLDS

    http_req_duration
    ✗ 'p(95)<500' p(95)=1.44s


  █ TOTAL RESULTS

    checks_total.......................: 11056   1096.887967/s
    checks_succeeded...................: 100.00% 11056 out of 11056
    checks_failed......................: 0.00%   0 out of 11056

    ✓ is 201 or 409

    CUSTOM
    coupon_201_count........................................................: 1000   99.212009/s
    coupon_201_rate.........................................................: 9.04%  1000 out of 11056
    coupon_409_count........................................................: 10056  997.675959/s
    coupon_409_rate.........................................................: 90.95% 10056 out of 11056
    coupon_other_rate.......................................................: 0.00%  0 out of 11056

    HTTP
    http_req_duration.......................................................: avg=742.98ms min=1.65ms med=724.83ms max=2.37s p(90)=1.23s    p(95)=1.44s
      { expected_response:true }............................................: avg=329.88ms min=4.86ms med=281.96ms max=1.44s p(90)=716.76ms p(95)=835.44ms
    http_req_failed.........................................................: 90.95% 10056 out of 11056
    http_reqs...............................................................: 11056  1096.887967/s

    EXECUTION
    iteration_duration......................................................: avg=743.18ms min=1.7ms  med=724.98ms max=2.37s p(90)=1.23s    p(95)=1.44s
    iterations..............................................................: 11056  1096.887967/s
    vus.....................................................................: 348    min=9              max=1476
    vus_max.................................................................: 1500   min=1500           max=1500

    NETWORK
    data_received...........................................................: 2.2 MB 214 kB/s
    data_sent...............................................................: 2.0 MB 202 kB/s
```

**RPS 측정 결과**

![coupon-issue-peak-test-rps](./load-test/coupon-issue-rps.png)

**요청 처리 시간**

![coupon-issue-peak-test-duration](./load-test/coupon-issue-duration.png)

- 최대 VU: 1,500
- 테스트 시간: 10초
- 총 쿠폰 발급 요청 수: 11,056

**성공률 분석**

- `201 Created` 응답 개수: 1,000건 (9.04%)
- `409 Conflict` 응답 개수: 1,000건의 요청 외에는 전부 발급 실패 응답 (90.95%)
- 기타 에러 0건

한정된 쿠폰 수량 1,000개에 정확히 맞아떨어지도록 1,000건의 요청만 성공했으며, 나머지 요청은 모두 정상적으로 발급에 실패했습니다.  
또한 별다른 서버 내부의 에러가 발생하지 않았습니다.  
이번 부하 테스트에서는 정확히 1,000건의 발급 요청이 성공하고 나머지는 수량 부족으로 인해 실패 처리됨을 검증하는 것이 목표였으므로, SLO 목표치였던 99%를 넘어 100%의 정상 응답률을 달성했다고 볼 수 있습니다.

**지연 시간 분석**

- P90: 1.23초
- P95: 1.44초

이번 부하 테스트에서 선정된 SLO는 P95 500ms 이하였으나, 실제 SLI는 P95 1.44초로, 무려 3배 가량의 차이가 존재했습니다.  
테스트에 대한 결과로 쿠폰 발급 로직의 안정성은 입증되었으나, 대규모 트래픽에 대한 성능 개선이 더욱 필요할 것으로 보입니다.

### 주문 및 결제 Load Test

**k6 실행 결과**

```sh
  █ THRESHOLDS

    http_req_duration
    ✓ 'p(90)<1000' p(90)=259.02ms

    http_req_failed
    ✓ 'rate<0.001' rate=0.02%


  █ TOTAL RESULTS

    checks_total.......................: 109696 365.61716/s
    checks_succeeded...................: 99.97% 109666 out of 109696
    checks_failed......................: 0.02%  30 out of 109696

    ✓ order created
    ✗ payment success
      ↳  99% — ✓ 54818 / ✗ 30

    CUSTOM
    payment_201_count.......................................................: 54818  182.70859/s
    payment_201_rate........................................................: 99.94% 54818 out of 54848
    payment_409_count.......................................................: 30     0.09999/s
    payment_409_rate........................................................: 0.05%  30 out of 54848
    payment_other_rate......................................................: 0.00%  0 out of 54848

    HTTP
    http_req_duration.......................................................: avg=117.47ms min=3.25ms  med=86.61ms  max=1.76s p(90)=259.02ms p(95)=341.3ms
      { expected_response:true }............................................: avg=117.46ms min=3.25ms  med=86.61ms  max=1.76s p(90)=258.99ms p(95)=341.22ms
    http_req_failed.........................................................: 0.02%  30 out of 109696
    http_reqs...............................................................: 109696 365.61716/s

    EXECUTION
    iteration_duration......................................................: avg=235.41ms min=10.33ms med=188.65ms max=2s    p(90)=489.02ms p(95)=609.93ms
    iterations..............................................................: 54848  182.80858/s
    vus.....................................................................: 11     min=1              max=100
    vus_max.................................................................: 100    min=100            max=100

    NETWORK
    data_received...........................................................: 35 MB  116 kB/s
    data_sent...............................................................: 24 MB  81 kB/s
```

**RPS 측정 결과**

![order-payment-rps](./load-test/order-payment-rps.png)

**초당 체크포인트 확인**

![order-payment-cps](./load-test/order-payment-cps.png)

**요청 처리 시간**

![order-payment-duration](./load-test/order-payment-duration.png)

- 최대 VU: 100
- 테스트 시간: 5분
- 총 요청 수: 109,696건
  - 주문 생성 요청 수: 54,848건
  - 결제 요청 수: 54,858건

**성공률 분석**

- `201 Created` (결제 성공) 응답 개수: 54,818건 (99.94%)
- `409 Conflict` (중복 결제) 응답 개수: 30건 (0.05%)
- 기타 에러 0건

테스트 결과 99.94%의 응답 성공률을 보였으며, 이는 SLO 목표치였던 99.9%를 만족하는 결과였습니다.  
또한 HTTP 409 실패 응답의 경우에는 로그를 확인해본 결과, 30건 모두 동일한 유저가 결제를 동시에 여러 번 진행하려고 한 케이스였으며, 이는 비정상적인 유즈 케이스이기 때문에 올바른 예외 처리였다고 볼 수 있습니다.

```sh
WARN[0248] 🚨 Payment failed: body={"code":"BALANCE_DEDUCT_FAILED","message":"사용자 잔액 차감 요청이 중복되어 결제를 진행할 수 없습니다."}  source=console
```

**지연 시간 분석**

- P90: 259.02ms
- P95: 341.3ms

주문 및 결제에 대해 선정된 SLO는 P90 1초였으나, 거의 1/4 수준인 P90 295.02ms를 달성했습니다.  
이로써 사용자가 100명 단위로 지속적으로 유지되는 상황에서도 안정적인 성능으로 API를 제공할 수 있게 되었습니다.

### SLI 지표 달성 여부

이번 부하 테스트는 앞서 말했듯 우리 서비스가 제공하고자 하는 SLO를 목표점으로 잡고 실제 SLI 계측을 통해 얼만큼의 서비스 가용성을 제공할 수 있는지를 확인하기 위한 목표로 진행되었습니다.  
해당 목표 아래 2가지 부하 테스트를 마쳤고, 아래는 목표한 SLO와 실제 SLI 수치를 비교한 내용입니다.

| 테스트 항목              | 측정 항목         | 목표 (SLO)                | 실제 결과 (SLI)     | 달성 여부 |
|--------------------------|------------------|---------------------------|----------------------|------------|
| **쿠폰 발급 Peak Test**  | Success Rate     | ≥ 99.0% (409 응답 포함)   | 100.00%              | ✅ 달성     |
|                          | Latency (P95)    | ≤ 500ms                   | **1.44s**            | ❌ 미달     |
| **주문 결제 Load Test**  | Success Rate     | ≥ 99.9%                   | 99.94%               | ✅ 달성     |
|                          | Latency (P90)    | ≤ 1s                      | 259.02ms             | ✅ 달성     |

전반적으로 시스템이 예상한 수준의 부하를 견딜 수 있다는 점이 확인되었습니다.  
하지만 선착순 쿠폰 발급의 경우에만 심각한 응답 지연 현상이 관측되므로 성능 개선이 시급한 상황입니다.  
따라서 후속 작업으로 쿠폰 발급 로직의 개선 혹은 인프라 구성의 scale-up을 고려해봐야 합니다.
