# 현재 구현 정의

## 목적

다수 PG 결제를 하나의 애플리케이션 API로 다루기 위한 Spring Boot 결제 서비스다. 현재 구현 PG는 Toss다.

## 런타임

- Java 21
- Spring Boot 3.2.4
- MySQL/H2
- Retrofit/OkHttp
- Thymeleaf
- gRPC member 예제

## 패키지

```text
com.wanted.clone.oneport
  core
  member
  payments
```

## payments

### Web

- `POST /order/new`: 주문 생성
- `GET /payment/checkout`: PG 결제 위젯 화면
- `GET /payment/success`: PG 승인 후 결과 화면
- `GET /payment/fail`: 실패 화면
- `POST /payment/confirm`: 로그 출력 후 실패 화면

### Application

- `OrderService`: 주문 생성, 주문 조회
- `PaymentService`: 결제 승인, PG adapter 선택, 결제 원장 저장
- `CancelService`: 결제 취소, 취소 원장 저장
- `PgWidgetService`: PG widget adapter 선택, template 경로 반환

### 결제/취소 규칙

- 결제 승인과 결제 취소는 주문 row를 비관적 쓰기 lock으로 조회한 뒤 처리한다.
- 같은 주문과 같은 `paymentKey`의 결제 승인 재요청은 성공으로 반환한다.
- 이미 결제된 주문에 다른 `paymentKey`로 승인 요청이 들어오면 `PaymentRuleViolationException`을 반환한다.
- 이미 `DONE` 원장이 있는 `paymentKey` 승인 요청은 PG 호출 전에 거부한다.
- 결제 승인 성공 시 주문 상태와 결제 원장을 저장한다.
- 결제 취소 성공 시 주문 상태와 취소 원장을 저장한다.
- 주문 결제 완료, 전체 취소, 부분 취소 상태 전이는 `Order` 도메인 메서드에서 처리한다.
- 부분 취소 시 선택된 주문 상품만 `ORDER_CANCELLED`로 바꾸고 주문은 `ORDER_PARTIAL_CANCELLED`가 된다.
- 모든 주문 상품이 취소되면 주문은 `ORDER_CANCELLED`가 된다.
- 취소 가능 금액보다 큰 취소 요청은 `PaymentRuleViolationException`으로 거부한다.
- 웹 계층은 `PaymentRuleViolationException`을 HTTP 400 `ErrorResponse`로 반환한다.
- lock wait timeout/deadlock 등 lock 획득 실패는 HTTP 409 `ErrorResponse`로 반환한다.
- 외부 PG 오류 응답은 `PgPaymentGatewayException`으로 변환하고 웹 계층은 HTTP 502 `ErrorResponse`로 반환한다.

### PG 선택

- `PaymentAPIs.provider()`와 `PgWidget.provider()`가 지원 `PgCorp`를 반환한다.
- `PaymentService`와 `PgWidgetService`는 `EnumMap<PgCorp, ...>`로 adapter를 선택한다.
- `pgCorpName` 문자열은 `PgCorp.from()`에서 정규화한다.
- 지원하지 않는 PG는 `UnsupportedPgCorpException`으로 거부한다.
- 웹 계층은 `UnsupportedPgCorpException`을 HTTP 400 `ErrorResponse`로 반환한다.

### 현재 adapter

- `TossPayment`: `PaymentAPIs`, `PgCorp.TOSS`
- `TossWidget`: `PgWidget`, `PgCorp.TOSS`
- `TossPaymentAPIs`: Retrofit interface
- Toss base URL, secret key, connect/write/read timeout, OkHttp connection retry 여부는 `pg.tosspayments` 설정으로 바인딩한다.
- Toss secret key는 `TOSS_PAYMENTS_SECRET_KEY` 환경변수로 주입할 수 있다.
- Toss Retrofit error body의 `code`, `message`는 공통 PG 예외로 변환한다.
- MySQL persistence adapter와 JPA repository
- `create_schema.sql`은 JPA entity 매핑 기준으로 `purchase_order.order_id`와 `order_items.order_id`를 `VARCHAR(255)`로 맞춘다.
- `order_items` 기본키는 JPA embedded id와 같은 `(order_id, item_idx)`다.
- 카드 결제 entity table은 `card_payment`, 기본키 column은 `payment_key`다.
- dev profile은 Docker init SQL로 schema를 생성하고 JPA `ddl-auto=validate`로 검증한다.
- test profile은 H2 테스트 격리를 위해 JPA `ddl-auto=create-drop`을 사용한다.
- `payment_ledger`는 `tx_id`, `method`, `payment_status` 조합으로 중복 원장 저장을 제한한다.

## 테스트

- ArchUnit 구조 테스트
- 주문 서비스 단위 테스트
- 결제 원장 JPA slice 테스트
- 주문 컨트롤러 REST Docs 테스트
- PG adapter/widget 선택 테스트
- Toss adapter error body 변환 테스트
- Toss adapter 승인/취소 성공 응답 MockWebServer 테스트
- `PgCorp.from()` 테스트
- 결제 승인 idempotency 테스트
- 결제 승인 실패 응답, lock 실패 전파 테스트
- 결제 취소 성공, 금액 검증 실패, lock 실패 전파 테스트
- 주문 취소 상태 전이 테스트
- 주문 payment id 일치성 도메인 테스트
- 취소 가능 금액 검증 테스트
- `findByIdForUpdate`의 `PESSIMISTIC_WRITE` lock 계약 테스트
- application/domain 의존 방향 ArchUnit 테스트
- REST Docs/OpenAPI order 생성 snippet 이름은 `order-new-success`, `order-new-validation-error`를 사용한다.

## 제한

- 주문 row lock은 JPA `PESSIMISTIC_WRITE`로 적용되어 있으나 H2 테스트만으로 MySQL lock wait 동작을 완전히 검증하지는 못한다.
- 외부 PG API 호출은 현재 트랜잭션 내부에서 수행되어 주문 row lock 보유 시간이 PG 응답 시간에 영향을 받는다.
- Toss 외부 API retry는 OkHttp의 연결 실패 재시도만 사용하며, 승인/취소 요청에 대한 애플리케이션 레벨 재시도는 하지 않는다.
- `payment_settlements` schema/entity 정리는 별도 단계로 남아 있다.

## Codex 하네스

- `/run {phase}` 또는 `/phase {phase}`는 `PLAN.md` 단계 하나를 완료한다.
- 예: `phase-02`는 `PLAN.md`의 `2단계` 전체 todo다.
- 구현/커밋은 `feature/...` 브랜치에서만 한다.
- phase 산출물은 `_workspace/codex/{phase}/implementation.md`, `verification.md`다.
- 계획은 phase별 `plan.md`가 아니라 루트 `PLAN.md`를 사용한다.
- 문서는 조건부로만 읽고 수정한다.
  - 동작 정의 변경: `SPEC.md`
  - 조사/판단 변경: `RESEARCH.md`
  - 실행/입문 변경: `README.md`
  - 계획 체크 변경: `PLAN.md`
- 검증은 기본 `.\gradlew.bat test` 하나로 한다.
- PR은 `gh pr create --base master --head {branch}`로 만든다.

## Persistence 모델 분리

- `Order`, `OrderItem`, `PurchaseOrderId`, `PaymentLedger`, `CardPayment`, `TransactionType`는 JPA 어노테이션과 persistence import가 없는 도메인 모델이다.
- MySQL JPA 매핑은 `infrastructure.persistence.mysql.entity` 하위의 `PurchaseOrderJpaEntity`, `OrderItemJpaEntity`, `PaymentLedgerJpaEntity`, `CardPaymentJpaEntity`가 담당한다.
- JPA enum converter는 `infrastructure.persistence.mysql.entity.converter` 하위에 둔다.
- repository adapter는 `OrderPersistenceMapper`, `PaymentPersistenceMapper`로 JPA entity와 도메인 모델을 변환한다.
- application port와 service는 저장소 구현체의 JPA entity를 직접 다루지 않는다.
- 검증은 핵심 도메인 모델 JPA 의존 금지 ArchUnit 테스트와 주문/결제 mapper 왕복 테스트로 수행한다.

## 추가 제한사항

- `PaymentSettlements`는 아직 JPA entity 형태로 domain 패키지에 남아 있다.
