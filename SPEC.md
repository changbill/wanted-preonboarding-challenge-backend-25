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

- `CreateNewOrderUseCase`
- `GetOrderInfoUseCase`
- `PgWidgetUseCase`
- `PaymentFullfillUseCase`
- `OrderCancelUseCase`
- `PaymentSettlementsUseCase`
- `SendSettlementsInfoUseCase`

### 애플리케이션 서비스

- `OrderService`
  - `ReqNewOrder`를 `Order` entity로 변환해 저장한다.
  - 주문 ID로 주문을 조회한다.
- `PaymentService`
  - 선택된 PG adapter로 결제 승인을 요청한다.
  - 승인 성공 시 주문 상태를 결제 완료로 변경하고 결제 원장을 저장한다.
  - 결제 원장에서 최신 결제 정보를 조회한다.
- `CancelService`
  - 주문과 결제 원장을 조회한다.
  - 취소 가능 금액과 주문 상태를 확인한다.
  - PG 취소 API 호출 후 취소 원장을 저장한다.
- `PgWidgetService`
  - PG 이름과 page type에 맞는 Thymeleaf template 경로를 반환한다.

### 출력 포트

- PG 연동
  - `PaymentAPIs`
  - `PgWidget`
- 저장소
  - `OrderRepository`
  - `PaymentLedgerRepository`
  - `PaymentSettlementsRepository`
  - `PaymentRepository`
  - `TransactionTypeRepository`

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
  - `TransactionType`
  - `CardPayment`
  - 카드/결제 enum converter
- 정산
  - `PaymentSettlements`

### 출력 adapter

- Toss PG
  - `TossPayment`: `PaymentAPIs` 구현
  - `TossWidget`: `PgWidget` 구현
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

- 결제 포트가 웹 DTO와 Toss DTO에 직접 의존한다.
- 도메인 모델 일부가 웹/Toss 타입에 직접 의존한다.
- Toss secret key와 base URL이 코드/설정에 하드코딩되어 있다.
- 결제 승인/취소 핵심 흐름 테스트가 부족하다.
- `create_schema.sql`과 JPA entity 매핑의 ID 타입/PK 구조가 일치하지 않을 가능성이 있다.
- `member` 패키지는 결제 패키지와 같은 아키텍처 경계를 사용하지 않는다.
