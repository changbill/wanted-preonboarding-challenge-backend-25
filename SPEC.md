# 현재 구현 구조

## 프로젝트 목적

다수 PG사의 결제 서비스를 하나의 API로 통합하기 위한 Spring Boot 기반 결제 서비스다. 현재 구현된 PG adapter는 Toss 중심이다.

## 런타임 구성

- Java 21
- Spring Boot 3.2.4
- MySQL 8.0.33 docker compose
- H2 test runtime
- Retrofit/OkHttp Toss API client
- Thymeleaf Toss 결제 화면
- gRPC member service

## 최상위 패키지

```text
com.wanted.clone.oneport
  core
  member
  payments
```

## `core`

- `ResponseWrapper`: REST 응답 래핑
- `GlobalException`: 전역 예외 처리
- `ApiResponse`, `ErrorResponse`: 공통 응답 형식
- `IdGenerator`: 주문 ID 생성 유틸리티
- `StaticRoutingConfiguration`: Swagger UI 정적 라우팅
- `CommonHttpMessageConverter`: 공통 응답 컨버터

## `payments`

### 입력 adapter

- `OrderController`
  - `POST /order/new`: 신규 주문 생성
  - `POST /order/test`: 요청 로그 테스트
  - `GET /order/info`: username echo 성격의 테스트 API
- `PaymentController`
  - `GET /payment/checkout`: 결제 위젯 화면 렌더링
  - `GET /payment/success`: 결제 승인 후 성공/실패 화면 렌더링
  - `GET /payment/fail`: 실패 화면 렌더링
  - `POST /payment/confirm`: 현재 로그 출력 후 고정 화면 반환

### 입력 포트

입력 포트는 `payments.application.port.in`에 있다. 웹 요청 DTO는 컨트롤러에서 application command로 변환한 뒤 입력 포트로 전달한다.

- `CreateNewOrderUseCase`
- `GetOrderInfoUseCase`
- `PgWidgetUseCase`
- `PaymentFullfillUseCase`
- `OrderCancelUseCase`
- `PaymentSettlementsUseCase`
- `SendSettlementsInfoUseCase`

### 애플리케이션 서비스

- `OrderService`
  - `CreateOrderCommand`를 `Order` entity로 변환해 저장한다.
  - 주문 ID로 주문을 조회한다.
- `PaymentService`
  - `ApprovePaymentCommand`로 결제 승인을 처리한다.
  - `PgCorp` 기준으로 선택된 PG adapter에 결제 승인을 요청한다.
  - 지원 adapter가 없는 `PgCorp`는 `UnsupportedPgCorpException`으로 거부한다.
  - 승인 성공 시 주문 상태를 결제 완료로 변경하고 결제 원장을 저장한다.
  - 결제 원장에서 최신 결제 정보를 조회한다.
- `CancelService`
  - `CancelPaymentCommand`로 결제 취소를 처리한다.
  - 주문과 결제 원장을 조회한다.
  - 취소 가능 금액과 주문 상태를 확인한다.
  - PG 취소 API 호출 후 취소 원장을 저장한다.
- `PgWidgetService`
  - `PgCorp` 기준으로 선택된 PG widget adapter와 page type에 맞는 Thymeleaf template 경로를 반환한다.
  - `pgCorpName` 문자열은 `PgCorp.from()`에서 대소문자와 하이픈 표기를 정규화한 뒤 enum으로 변환한다.
  - 지원 widget adapter가 없는 `PgCorp`는 `UnsupportedPgCorpException`으로 거부한다.

### 출력 포트

- PG 연동
  - `PaymentAPIs`: `provider()`로 지원 PG를 명시하고, application command를 받아 application result를 반환한다.
  - `PgWidget`: `provider()`로 지원 PG를 명시하고, PG별 template 경로를 반환한다.
- 저장소
  - `OrderRepository`
  - `PaymentLedgerRepository`
  - `PaymentSettlementsRepository`
  - `PaymentRepository`
  - `TransactionTypeRepository`

### 예외 응답

- 지원하지 않는 PG 이름 또는 adapter 없는 PG 요청은 `UnsupportedPgCorpException`으로 처리한다.
- 웹 요청에서 해당 예외가 발생하면 HTTP 400 `BAD_REQUEST`와 `ErrorResponse` 본문을 반환한다.

### 도메인/JPA 모델

- 주문
  - `Order`
  - `OrderItem`
  - `PurchaseOrderId`
  - `OrderStatus`
  - `OrderStatusConverter`
- 결제
  - `PaymentLedger`
  - `PaymentMethod`
  - `PaymentStatus`
  - `PgCorp`
  - `TransactionType`
  - `CardPayment`
  - 카드/결제 enum converter
- 정산
  - `PaymentSettlements`

### 출력 adapter

- Toss PG
  - `TossPayment`: `PaymentAPIs` 구현, `PgCorp.TOSS` 지원
  - `TossWidget`: `PgWidget` 구현, `PgCorp.TOSS` 지원
  - `TossPaymentAPIs`: Retrofit interface
  - `TossApiClientConfig`: Retrofit/OkHttp bean 구성
  - Toss request/response DTO
- MySQL persistence
  - `OrderRepositoryImpl`
  - `PaymentTransactionLedgerRepository`
  - `CardTransactionTypeRepository`
  - JPA repository interfaces

## `member`

- `Member`: JPA entity
- `MemberRepository`: Spring Data JPA repository
- `MemberService`: 회원 생성 서비스
- `MemberServiceGrpcImpl`: gRPC adapter
- `MemberMapper`: MapStruct mapper
- `MemberSignUpRequestDTO`: 회원 생성 DTO

## 테스트 구조

- `ArchTests`, `Arch2Tests`: ArchUnit 규칙
- `OrderServiceTests`: 주문 생성 서비스 단위 테스트
- `PaymentLedgerRepositoryTests`: JPA repository slice 테스트
- `OrderControllerDocTest`: REST Docs 기반 주문 API 문서 테스트
- `IdGeneratorTests`: ID 생성기 테스트
- `OnePortApplicationTests`: Spring context load 테스트

## 알려진 제한

- 입력 adapter의 웹 요청 DTO가 application command로 변환되는 책임을 가진다.
- Toss secret key와 base URL이 코드/설정에 하드코딩되어 있다.
- 결제 승인/취소 핵심 흐름 테스트가 부족하다.
- `create_schema.sql`과 JPA entity 매핑의 ID 타입/PK 구조가 일치하지 않을 가능성이 있다.
- `member` 패키지는 결제 패키지와 같은 아키텍처 경계를 사용하지 않는다.

## Codex 하네스

- 사용자는 `/run {phase}` 또는 `/phase {phase}`로 phase를 시작한다.
- phase 기준은 `PLAN.md`의 단계다. 예를 들어 `phase-02`는 `PLAN.md`의 `2단계` todo 전체를 완료하는 단위다.
- 각 phase의 구현과 커밋은 `feature/...` 브랜치에서만 수행한다.
- 오케스트레이터는 `master` 또는 `main`에서 바로 수정하지 않고 `feature/{번호}-{작업요약}` 브랜치를 만든 뒤 진행한다.
- 오케스트레이터는 내부적으로 기획, 구현, 검증, 수정, 커밋을 수행한다.
- phase 산출물과 Codex/하네스 설정 파일은 검증된 코드 변경과 같은 커밋에 포함한다.
- phase 완료 후 오케스트레이터는 feature 브랜치를 push하고 `master` 대상 PR 생성을 시도한다.
- PR 생성 도구, 인증, 원격 저장소 설정 문제로 자동 생성이 불가능하면 `_workspace/codex/{phase}/pr.md`에 PR 초안을 남기고 최종 보고에 이유를 기록한다.
- 하네스 상태는 `_workspace/codex/state.json`에 저장한다.
- 단계별 산출물은 `_workspace/codex/{phase}/` 아래에 둔다.
- 주요 산출물은 `plan.md`, `implementation.md`, `verification.md`다.
- `.agents/harness/guard.ps1`은 phase 시작/완료와 내부 단계 전이를 검사한다.
- `.agents/harness/set-state.ps1`은 phase, status, last command, updated timestamp를 갱신한다.
- 커밋 전 hook은 `feature/...` 브랜치, `verifying`/`committing`/`committed` 상태, `verification.md`의 `gradlew.bat test: PASS` 기록, 필수 산출물과 설정 파일의 staged 상태를 요구한다.
