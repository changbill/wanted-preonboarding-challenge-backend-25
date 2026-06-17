# 조사 기록

## 기준 자료

- Hexagonal Architecture: https://alistair.cockburn.us/hexagonal-architecture/
- ArchUnit User Guide: https://www.archunit.org/userguide/html/000_Index.html
- Spring Modulith Reference: https://docs.spring.io/spring-modulith/reference/fundamentals.html

## 현재 판단

- 결제 도메인은 `presentation`, `application`, `domain`, `infrastructure`로 나뉘어 있다.
- 포트는 애플리케이션 소유 타입을 사용해야 한다. 웹 DTO와 Toss DTO는 adapter 경계 밖으로 새지 않아야 한다.
- PG 선택은 클래스명 규칙이 아니라 `PaymentAPIs.provider()`, `PgWidget.provider()`가 반환하는 `PgCorp`로 한다.
- 외부 입력의 `pgCorpName`은 `PgCorp.from(String)`에서 정규화한다.
- 지원하지 않는 PG는 `UnsupportedPgCorpException`으로 거부하고 웹에서는 HTTP 400으로 응답한다.
- singleton service에 요청별 PG adapter 상태를 필드로 저장하지 않는다.
- 결제 승인과 취소는 같은 주문 row를 기준으로 경합하므로 application port에 lock 조회 계약을 두고 JPA adapter에서 `PESSIMISTIC_WRITE`를 적용한다.
- 현재 주문 상태에는 `PAYMENT_APPROVING` 같은 진행 중 상태가 없다. 새 상태를 넣으면 상태 전이와 사용자 노출 의미가 커지므로 phase-05에서는 도입하지 않는다.
- lock wait timeout/deadlock은 Spring의 lock 획득 실패 예외로 처리하고 웹 계층에서 HTTP 409로 응답한다.
- 결제 원장 중복 방지는 `payment_ledger(tx_id, method, payment_status)` unique constraint로 둔다. 기존 `(id, tx_id, method, payment_status)` unique key는 auto increment id 때문에 중복 방지 효과가 없다.

## 남은 리스크

- 결제 취소 idempotency 규칙은 아직 별도로 정의되어 있지 않다.
- Toss 설정은 `@ConfigurationProperties`로 외부화했고 secret은 환경변수로 주입할 수 있다.
- `create_schema.sql`의 `order_items.order_id`는 JPA `String` FK와 맞게 `VARCHAR(255)`로 둔다.
- `order_items` PK는 JPA `PurchaseOrderJpaId(orderId, itemIdx)`와 맞게 `(order_id, item_idx)`로 둔다.
- `card_payment` SQL table/PK는 JPA `CardPaymentJpaEntity`와 맞게 `card_payment(payment_key)`로 둔다.
- dev DB는 Docker init SQL로 schema를 만들고 JPA `ddl-auto=validate`로 매핑 정합성만 검증한다.
- test DB는 H2 isolated schema 생성을 위해 `ddl-auto=create-drop`을 유지한다.
- H2 테스트만으로 MySQL lock 동작을 검증할 수 없다.
- 외부 PG API 호출 중 트랜잭션이 주문 row lock을 보유한다.
- 결제 승인/취소 API는 중복 호출 부작용이 클 수 있으므로 애플리케이션 레벨 자동 retry는 도입하지 않는다. 현재는 OkHttp의 연결 실패 재시도(`retryOnConnectionFailure`)만 설정으로 제어한다.
- Toss Retrofit error body는 `{code, message}` 형태를 공통 `PgPaymentGatewayException`으로 변환하고 웹 계층에서 HTTP 502로 반환한다.

## 다음 조사 우선순위

1. 결제 취소 idempotency 규칙
2. 주문 row 기준 비관적 락의 MySQL 통합 테스트
3. JPA schema 정합성
