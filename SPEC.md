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
- MySQL persistence adapter와 JPA repository

## 테스트

- ArchUnit 구조 테스트
- 주문 서비스 단위 테스트
- 결제 원장 JPA slice 테스트
- 주문 컨트롤러 REST Docs 테스트
- PG adapter/widget 선택 테스트
- `PgCorp.from()` 테스트

## 제한

- 결제 승인/취소 idempotency가 없다.
- 주문/결제 동시성 제어가 없다.
- Toss 설정이 외부화되어 있지 않다.
- JPA schema 정합성 검증이 남아 있다.

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
