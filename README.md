# 분석 및 설계

[Wiki 페이지에서 확인하기](https://github.com/nmin11/hhplus-e-commerce/wiki)

## ⏰ 마일스톤

- [📅 프로젝트 칸반 보드](https://github.com/users/nmin11/projects/1/views/4?sortedBy%5Bdirection%5D=asc&sortedBy%5BcolumnId%5D=180643321)
- [🚀 마일스톤 목록](https://github.com/nmin11/hhplus-e-commerce/milestones)

## 🔁 시퀀스 다이어그램

### 사용자 잔액 충전

```mermaid
sequenceDiagram
  actor User
  participant Balance
  participant BalanceHistory

  User->>Balance: 금액 충전 요청 (userId, amount)
  activate Balance

  Balance->>Balance: 현재 금액 + 충전 금액 계산 및 저장
  Balance->>BalanceHistory: 충전 기록 생성 요청 (userId, amount, now)
  activate BalanceHistory
  BalanceHistory-->>Balance: 기록 완료
  deactivate BalanceHistory

  Balance-->>User: 충전 결과 반환
  deactivate Balance
```

<br/>

### 사용자 잔액 조회

```mermaid
sequenceDiagram
  actor User
  participant Balance

  User->>Balance: 현재 잔액 조회 요청 (userId)
  activate Balance

  Balance-->>User: 현재 잔액 반환
  deactivate Balance
```

<br/>

### 사용자 잔액 변경 내역 조회

```mermaid
sequenceDiagram
  actor User
  participant BalanceHistory

  User->>BalanceHistory: 잔액 변경 내역 조회 요청 (userId)
  activate BalanceHistory

  BalanceHistory-->>User: 잔액 변경 내역 목록 반환
  deactivate BalanceHistory
```

<br/>

### 상품 목록 조회

```mermaid
sequenceDiagram
  actor User
  participant Product

  User->>Product: 상품 목록 조회 요청
  activate Product

  Product-->>User: 상품 목록 반환
  deactivate Product
```

<br/>

### 개별 상품 상세 조회

```mermaid
sequenceDiagram
  actor User
  participant Product
  participant ProductOption

  User->>Product: 상품 상세 조회 요청 (productId)
  activate Product

  Product->>ProductOption: 해당 상품 옵션 목록 요청
  activate ProductOption
  ProductOption-->>Product: 옵션 목록 반환
  deactivate ProductOption

  Product-->>User: 상품 상세 정보 + 옵션 목록 반환
  deactivate Product
```

<br/>

### 주문

```mermaid
sequenceDiagram
  actor User
  participant Order
  participant Product
  participant Stock

  User->>Order: 주문 요청 (userId, items)
  activate Order

  
  Order->>Product: 상품 옵션 및 재고 확인
  activate Product

  loop 각 주문 상품들
    Product->>Stock: 옵션에 해당하는 재고 확인 요청
    activate Stock

    alt 재고 충분
      Stock-->>Product: 재고 확인 완료
    else 재고 부족
      Stock-->>Product: 재고 부족 오류
    end
    deactivate Stock
  end

  Product-->>Order: 상품 옵션 및 재고 확인 완료
  deactivate Product

  Order->>Order: 주문서 생성
  Order-->>User: 주문 생성 완료 응답
  deactivate Order
```

<br/>

### 결제

```mermaid
sequenceDiagram
  actor User
  participant Payment
  participant Order
  participant Coupon
  participant Product
  participant Balance
  participant Statistic
  participant Data Platform

  User->>Payment: 결제 요청 (orderId, couponId)
  activate Payment

  Payment->>Order: 주문 상태 확인
  activate Order
  Order-->>Payment: 주문 정보 및 상태 반환
  deactivate Order

  opt 쿠폰이 있는 경우
    Payment->>Coupon: 쿠폰 유효성 및 사용 여부 확인
    activate Coupon

    alt 쿠폰이 유효
      Coupon-->>Payment: 결제 금액 차감
    else 쿠폰이 사용 불가
      Coupon-->>Payment: 쿠폰 사용 불가 오류
    end

    deactivate Coupon
  end

  Payment->>Product: 주문 항목의 재고 확인 요청
  activate Product

  alt 재고 충분
    Product-->>Payment: 재고 검증 완료
  else 재고 부족
    Product-->>Payment: 재고 부족 오류
  end
  deactivate Product

  Payment->>Balance: 잔액에서 결제 금액 차감
  activate Balance

  alt 잔액 충분
    Balance-->>Payment: 잔액 차감 완료
  else 잔액 부족
    Balance-->>Payment: 잔액 부족 오류
  end
  deactivate Balance

  Payment->>Payment: 결제 정보 생성 및 저장

  Payment-->>Statistic: 통계 반영 요청 (비동기)
  Payment-->>Data Platform: 주문 데이터 전송 (비동기)

  Payment-->>User: 결제 성공 응답
  deactivate Payment
```

<br/>

### 선착순 쿠폰 발급 요청

```mermaid
sequenceDiagram
  actor User
  participant Coupon

  User->>Coupon: 쿠폰 발급 요청 (userId, couponId)
  activate Coupon

  Coupon->>Coupon: 사용자 보유 여부 확인
  Coupon->>Coupon: 쿠폰 잔여 수량 확인

  alt 사용자가 쿠폰을 갖고 있지 않고, 쿠폰 수량이 남아있는 경우
    Coupon->>Coupon: 쿠폰 수량 차감
    Coupon-->>User: 사용자에게 쿠폰 발급
  else 이미 보유한 쿠폰 or 수량 부족
    Coupon-->>User: 쿠폰 발급 실패 응답
  end

  deactivate Coupon
```

<br/>

### 보유 쿠폰 조회

```mermaid
sequenceDiagram
  actor User
  participant Coupon

  User->>Coupon: 보유 쿠폰 목록 조회 요청 (userId)
  activate Coupon

  Coupon-->>User: 사용자 보유 쿠폰 목록 반환
  deactivate Coupon
```

<br/>

### 최근 인기 상품 조회

```mermaid
sequenceDiagram
  actor User
  participant Statistic

  User->>Statistic: 인기 상품 조회 요청 (최근 3일)
  activate Statistic

  Statistic-->>User: 인기 상품 목록 (TOP 5) 반환
  deactivate Statistic
```

## 🧩 클래스 다이어그램

<img src="docs/class-diagram.svg">

<a href="https://excalidraw.com/#json=Hc605C7zlAejmaIcTEZ-x,OcDHW0BjR6G1ap45K_0_bQ" target="_blank">
    웹에서 클래스 다이어그램 확인하기
</a>

## ⚒️ ERD

<img src="docs/erd.svg">

<a href="https://dbdiagram.io/d/hhplus-e-commerce-67ebd1d24f7afba184ef6b5b" target="_blank">
    웹에서 ERD 확인하기
</a>
