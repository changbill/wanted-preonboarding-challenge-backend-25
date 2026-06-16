# 프로젝트 구조 및 헥사고날 아키텍처 조사

## 조사 범위

- `README.md`, `build.gradle.kts`, `docker-compose.yml`, `application.yml`
- `src/main/java/com/wanted/clone/oneport`
- `src/test/java/com/wanted/clone/oneport`
- 헥사고날 아키텍처 원문 및 ArchUnit 문서

## 외부 자료

- Alistair Cockburn, "Hexagonal architecture the original 2005 article"
  - URL: https://alistair.cockburn.us/hexagonal-architecture/
  - 핵심: 애플리케이션을 UI나 DB 없이도 실행/테스트할 수 있게 만들고, 외부 기술은 포트 뒤의 어댑터로 교체 가능하게 둔다.
  - 포트는 목적 기반 API이며, 어댑터는 HTTP, GUI, DB, mock, batch, 외부 시스템 같은 기술별 변환 계층이다.
  - 핵심 비대칭은 계층의 위/아래가 아니라 애플리케이션 내부와 외부의 경계다.
- ArchUnit User Guide
  - URL: https://www.archunit.org/userguide/html/000_Index.html
  - 핵심: 패키지 의존, 레이어, 어니언 아키텍처 규칙을 테스트 코드로 고정할 수 있다.
- Spring Modulith Reference
  - URL: https://docs.spring.io/spring-modulith/reference/fundamentals.html
  - 핵심: Spring 애플리케이션 내부의 기능 모듈 경계를 정의하고 검증할 수 있다.

## 현재 프로젝트 개요

이 프로젝트는 Spring Boot 3.2.4, Java 21 기반의 PG 통합 결제 API 사전과제 구현이다. 주요 기능은 주문 생성, 결제 위젯 렌더링, Toss 결제 승인, 결제 취소, 결제 원장 저장이다. 추가로 gRPC 기반 `member` 예제가 포함되어 있다.

주요 외부 기술은 다음과 같다.

- Web MVC, Thymeleaf
- Spring Data JPA, MySQL, H2
- Retrofit, OkHttp
- gRPC, Protobuf
- Spring REST Docs, restdocs-api-spec, Swagger UI 정적 리소스
- ArchUnit

## 패키지 구조 분석

### `core`

- `common`: 공통 응답 래핑, 예외 응답, ID 생성기
- `config`: 정적 라우팅, 공통 HTTP 메시지 컨버터

공통 웹 응답과 설정 성격의 코드가 모여 있다.

### `payments`

결제 도메인은 다음 계층으로 나뉜다.

- `presentation.web`: Spring MVC 컨트롤러, 웹 요청/응답 DTO
- `presentation.port.in`: 유스케이스 인터페이스
- `application.service`: 주문, 결제 승인, 결제 취소, PG 위젯 서비스
- `application.port.out`: PG API, PG 위젯, 저장소 포트
- `domain.entity`: 주문, 주문상품, 결제원장, 카드결제, 정산 엔티티와 enum/converter
- `infrastructure.pg.toss`: Toss API Retrofit client, Toss 요청/응답 DTO, Toss adapter
- `infrastructure.persistence.mysql`: JPA repository와 포트 구현체
- `infrastructure.persistence.oracle`: 빈 구현체 성격의 placeholder

이름과 큰 방향은 헥사고날/클린 아키텍처를 의식한 구조다. 입력 포트와 출력 포트가 있고, Toss와 MySQL 구현이 바깥 어댑터로 배치되어 있다.

### `member`

`Member`, `MemberRepository`, `MemberService`, `MemberServiceGrpcImpl`, `MemberMapper`, DTO가 하나의 패키지에 평면적으로 놓여 있다. 결제 도메인과 달리 포트/어댑터 경계가 없다.

## 현재 구조의 장점

- 결제 도메인은 `presentation`, `application`, `domain`, `infrastructure` 패키지를 나누고 있어 아키텍처 의도가 드러난다.
- PG 연동은 `PaymentAPIs`, `PgWidget` 인터페이스를 통해 Toss 외 PG 추가를 고려한 형태다.
- 저장소는 `OrderRepository`, `PaymentLedgerRepository`, `TransactionTypeRepository` 같은 출력 포트를 두고 MySQL 구현체가 이를 구현한다.
- `Order`, `OrderItem`, `PaymentLedger`에 상태 변경 메서드가 있어 모든 로직이 서비스에만 몰려 있지는 않다.
- ArchUnit 테스트가 도입되어 컨트롤러 네이밍, 포트/서비스 의존 규칙 일부를 검증하려는 시도가 있다.
- REST Docs와 OpenAPI 생성 설정이 있어 API 문서화 기반이 있다.

## 현재 구조의 문제

### 포트 타입이 외부 계층에 오염됨

`PaymentAPIs` 출력 포트가 `ReqPaymentApprove`, `ReqCancelOrder` 같은 웹 요청 DTO와 `TossCancelResponseMessage`, `TossSettlementsResponseMessage` 같은 Toss 전용 응답 타입을 직접 노출한다.

영향:

- 애플리케이션 서비스가 웹 DTO와 Toss DTO를 알아야 한다.
- 다른 PG를 추가할 때 포트의 공통 계약이 아니라 Toss 중심 계약을 맞추게 된다.
- mock adapter를 만들기 어렵고 테스트가 외부 타입에 묶인다.

### 도메인이 외부 타입을 참조함

`PaymentLedger`, `PgCorpConverter`, `PaymentApproveResponse`, `TransactionType`, `CardPayment`에서 웹 패키지의 `PgCorp` 또는 Toss 응답 타입을 참조한다.

영향:

- 도메인 모델이 결제사의 응답 포맷 변화에 영향을 받는다.
- 도메인 규칙과 외부 메시지 변환 책임이 섞인다.
- 헥사고날 아키텍처의 내부/외부 경계가 약해진다.

### 입력 포트 위치가 애매함

입력 포트가 `payments.presentation.port.in` 아래에 있다. 유스케이스는 컨트롤러가 제공하는 것이 아니라 애플리케이션이 외부에 제공하는 API이므로 `application.port.in`에 두는 편이 의존 방향을 더 명확하게 만든다.

### 애플리케이션 서비스가 상태를 가졌던 문제

이전에는 `PaymentService`에 `public PaymentAPIs paymentAPIs` 필드가 있고 요청 처리 중 선택된 PG API를 필드에 저장했다.

해결:

- 선택된 PG API는 `paymentApproved()` 안의 지역 변수로만 사용한다.
- singleton Spring bean에 요청별 PG adapter 상태를 남기지 않는다.

### PG 선택 방식이 클래스명 규칙에 의존했던 문제

이전에는 `PaymentService`, `PgWidgetService`가 bean class name을 `split("Payment")`, `split("Widget")`으로 잘라 map key를 만들었다.

해결:

- `PaymentAPIs.provider()`와 `PgWidget.provider()` 계약을 추가했다.
- `TossPayment`, `TossWidget`은 `PgCorp.TOSS`를 반환한다.
- `PaymentService`는 `EnumMap<PgCorp, PaymentAPIs>`로 adapter를 선택한다.
- `PgWidgetService`는 `EnumMap<PgCorp, PgWidget>`로 widget adapter를 선택한다.
- `PgCorp.from(String)`이 외부 입력의 대소문자와 하이픈 표기를 정규화해 PG 식별자로 변환한다.
- 지원하지 않는 PG 이름이나 adapter 없는 PG는 `UnsupportedPgCorpException`으로 명시하고, 웹 계층은 HTTP 400 `ErrorResponse`로 응답한다.
- 클래스명 변경이 PG 선택 로직을 깨뜨리지 않는다.

### 결제 취소가 Toss 타입에 묶임

`CancelService`는 `TossCancelResponseMessage`를 직접 사용한다. 출력 포트가 공통 취소 결과를 반환하면 서비스는 PG와 무관하게 동작할 수 있다.

### 설정과 secret 관리가 미흡함

`application.yml`과 `TossApiClientConfig`에 Toss secret key가 하드코딩되어 있고, config의 `BASE_URL`, `SECRET_KEY`는 profile 설정값을 사용하지 않는다.

영향:

- 운영/개발/테스트 환경 분리가 어렵다.
- secret 노출 위험이 있다.
- 테스트에서 fake endpoint나 MockWebServer를 붙이기 어렵다.

### DB schema와 JPA 매핑 불일치 가능성

`create_schema.sql`의 `order_items.order_id`는 `BINARY(16)`인데 `Order.orderId`는 `String`이고 `JpaOrderRepository`는 `JpaRepository<Order, String>`이다. `OrderItem`은 `PurchaseOrderId` embedded id를 쓰지만 SQL의 PK는 `(id, item_idx)` 형태다.

영향:

- Hibernate ddl-auto와 직접 schema가 서로 다른 구조를 만들 수 있다.
- 실제 MySQL 초기화 schema 기준으로 실행하면 매핑 오류가 날 수 있다.

### 테스트가 핵심 결제 흐름을 충분히 보호하지 못함

현재 테스트는 아키텍처 일부, ID 생성기, 주문 서비스, JPA 저장, 주문 컨트롤러 문서 테스트 중심이다. 결제 승인/취소 상태 전이, PG 실패, 중복 승인, 부분 취소, 외부 API timeout, idempotency 검증은 부족하다.

### 동시성 제어가 없음

현재 결제 승인과 취소는 동일 주문 또는 동일 결제키에 대해 동시에 요청될 수 있다. Toss 성공 콜백, 사용자의 재시도, 네트워크 재전송, 클라이언트 중복 클릭이 겹치면 같은 주문에 결제 완료 처리가 두 번 들어가거나 결제 원장이 중복 저장될 수 있다.

특히 `PaymentService`는 singleton bean인데 요청 처리 중 선택된 PG API를 `paymentAPIs` 필드에 저장한다. 이 값은 요청별 지역 변수여야 하며, 동시 요청에서 공유 mutable state가 되면 다른 요청의 PG 선택에 영향을 줄 수 있다.

락이 필요한 주요 자원은 다음과 같다.

- `purchase_order`: 주문 결제 상태와 취소 상태
- `payment_ledger`: 결제키 기준 승인/취소 원장
- `order_items`: 부분 취소 상태

### 문서 체계가 초기 과제 README 중심임

`README.md`는 사전과제 안내에 가깝고, 현재 구현 구조/운영 방법/제약을 분리한 `SPEC.md`, 조사 근거인 `RESEARCH.md`, 개선 계획인 `PLAN.md`가 없었다.

## 헥사고날 아키텍처 관점의 목표 구조

권장 의존 방향:

```text
external adapters
  -> application ports
      -> application services
          -> domain model
      -> output ports
  <- infrastructure adapters
```

결제 도메인 기준 권장 패키지:

```text
payments
  domain
    model
    policy
  application
    port
      in
      out
    service
    command
    result
  adapter
    in
      web
      grpc
    out
      pg
        toss
      persistence
        mysql
```

이 프로젝트에 바로 적용할 때는 전체 패키지를 한 번에 갈아엎기보다, 타입 경계부터 정리하는 편이 안전하다.

## 락 구현 조사

### 후보

#### JVM local lock

`synchronized`, `ReentrantLock`, key-based local lock으로 같은 프로세스 안의 중복 요청은 막을 수 있다.

장점:

- 구현이 단순하다.
- 외부 인프라가 필요 없다.

단점:

- 서버 인스턴스가 2대 이상이면 깨진다.
- DB transaction과 직접 결합되지 않는다.
- 애플리케이션 재시작이나 장애 상황에서 보호 범위가 사라진다.

이 프로젝트의 결제 상태는 DB row가 최종 공유 자원이므로 적합하지 않다.

#### Optimistic Lock

JPA `@Version` 필드를 `Order`, `PaymentLedger` 등에 추가하고 update 충돌 시 재시도 또는 실패 처리한다.

장점:

- 락 대기 시간이 없다.
- 충돌이 드문 일반 수정 흐름에 적합하다.

단점:

- 결제 승인/취소처럼 같은 주문에 대한 중복 요청을 반드시 직렬화해야 하는 흐름에서는 충돌 후 보상/재시도 설계가 필요하다.
- 외부 PG API 호출과 함께 쓰면 충돌 시 이미 외부 결제가 승인된 상태를 처리해야 한다.

보조 수단으로는 좋지만 1차 선택으로는 부족하다.

#### DB Pessimistic Lock

JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)` 또는 `select ... for update`로 주문 row를 잠그고 같은 주문의 승인/취소 처리를 직렬화한다.

장점:

- 여러 서버 인스턴스에서도 같은 DB를 공유하면 동작한다.
- 주문 상태 변경과 같은 transaction 안에서 일관성을 보장하기 쉽다.
- 현재 MySQL/JPA 구조에 가장 자연스럽게 붙일 수 있다.

단점:

- 외부 PG API 호출 중 DB lock을 오래 잡으면 대기와 timeout 위험이 있다.
- deadlock과 lock wait timeout 정책을 정해야 한다.

이 프로젝트에서는 1차 선택지로 적합하다.

#### Redis Distributed Lock

Redis에 `orderId` 또는 `paymentKey` 기준 분산락을 잡고 결제/취소 처리를 직렬화한다.

장점:

- DB row lock보다 외부 API 호출 구간까지 넓게 보호하기 쉽다.
- 여러 DB 자원을 묶는 coarse-grained lock을 만들기 쉽다.

단점:

- Redis 운영 의존성이 추가된다.
- lock lease time, renewal, 장애 시 해제 정책이 필요하다.
- DB unique constraint와 idempotency 없이 단독으로 쓰면 최종 정합성을 보장하기 어렵다.

현재 프로젝트 규모에서는 DB pessimistic lock과 unique constraint를 먼저 적용하고, 멀티 인스턴스/고부하에서 DB lock 대기가 문제가 될 때 검토하는 편이 낫다.

### 권장 방식

1차 구현은 DB row lock 기반 비관적 락을 적용한다.

- `JpaOrderRepository.findByIdForUpdate(orderId)` 추가
- application repository port에도 lock 조회 메서드 추가
- 결제 승인/취소 service에서 transaction 안에 locked order를 조회
- `PaymentService.paymentAPIs` 필드는 제거하고 지역 변수로 변경
- 결제 원장에는 중복 저장 방지 unique constraint를 추가
- 같은 `paymentKey`의 동일 승인 요청은 idempotent 성공으로 처리

외부 PG API 호출은 DB lock 안에서 오래 수행하지 않는 편이 좋다. 더 안전한 흐름은 주문을 짧게 잠그고 `PAYMENT_APPROVING` 같은 진행 상태로 바꾼 뒤 commit, PG 승인 호출 후 다시 row lock을 잡고 최종 상태를 확정하는 방식이다. 이 방식은 lock 유지 시간을 줄이지만, `PAYMENT_APPROVING` 상태의 timeout/retry/recovery 정책을 함께 정의해야 한다.

초기 구현에서는 다음 두 방식 중 선택이 필요하다.

- 단순 구현: transaction 안에서 row lock을 잡고 PG API 호출까지 수행한다.
- 권장 구현: 짧은 lock으로 진행 상태를 선점하고, 외부 호출 후 재락을 잡아 확정한다.

결제 서비스에서는 외부 API latency와 실패 가능성이 있으므로 권장 구현이 더 낫다.

### 락 테스트 방식

락 테스트는 H2보다 MySQL 계열 DB에서 실행해야 한다. H2의 lock 동작은 MySQL `select for update`와 다를 수 있으므로 Testcontainers MySQL 또는 docker compose MySQL을 사용한다.

검증 항목:

- `findByIdForUpdate`가 같은 주문 row에 대해 동시 update를 직렬화한다.
- 동일 `orderId` 결제 승인 요청 2개를 동시에 실행해도 주문은 한 번만 결제 완료가 된다.
- 동일 `paymentKey` 승인 원장이 1건만 저장된다.
- 승인과 취소가 동시에 들어와도 정의된 상태 전이만 남는다.
- lock wait timeout 또는 deadlock 발생 시 사용자에게 정의된 오류를 반환한다.

테스트 구현은 `CountDownLatch`와 `ExecutorService`로 여러 스레드를 동시에 출발시키는 방식이 적합하다. 테스트 메서드 자체에는 `@Transactional`을 붙이지 않고, service 메서드에 transaction 경계를 둬야 스레드별 transaction과 DB lock 동작을 제대로 검증할 수 있다.

## 개선 우선순위

1. 포트 계약 정리
   - 웹 DTO와 Toss DTO를 `application.port` 밖으로 밀어낸다.
   - `ApprovePaymentCommand`, `CancelPaymentCommand`, `PaymentApprovalResult`, `PaymentCancelResult` 같은 애플리케이션 타입을 만든다.
2. 도메인 외부 타입 제거
   - `PgCorp`를 도메인 또는 애플리케이션 공통 enum으로 이동한다.
   - Toss 응답에서 `CardPayment`를 만드는 변환 코드는 Toss adapter 또는 mapper에 둔다.
3. 입력 포트 위치 이동
   - `presentation.port.in`을 `application.port.in`으로 이동한다.
4. PG adapter 선택 명시화
   - `PaymentAPIs`에 `supports(PgProvider provider)` 또는 `provider()`를 추가한다.
   - 클래스명 split 기반 selector를 제거한다.
5. secret/config 외부화
   - `@ConfigurationProperties`로 Toss base URL, secret key, timeout을 profile/env에서 주입한다.
6. 상태 전이와 금액 검증 도메인화
   - 주문 완료, 결제 완료, 전체/부분 취소, 구매 확정 이후 취소 불가 규칙을 도메인 메서드와 테스트로 고정한다.
7. 결제/취소 동시성 제어
   - 주문 row 기준 DB pessimistic lock을 적용한다.
   - 결제 원장 unique constraint와 idempotent 처리를 추가한다.
   - 외부 PG API 호출 중 lock 유지 시간을 줄이는 진행 상태 선점 방식을 검토한다.
8. schema/JPA 정합성 정리
   - SQL schema와 JPA ID 타입, PK/FK 구조를 맞춘다.
9. 테스트 보강
   - 결제 승인/취소 application service 단위 테스트
   - 결제 승인/취소 동시성 테스트
   - Toss adapter MockWebServer 테스트
   - repository slice test
   - ArchUnit 의존 규칙 강화

## 결론

현재 코드는 "헥사고날 아키텍처를 향해 나누기 시작한 Spring Boot 결제 서비스"에 가깝다. 패키지 이름과 포트 인터페이스는 방향이 좋지만, 포트 계약과 도메인 타입이 웹/Toss/JPA 세부사항을 직접 참조하고 있어 실제 교체 가능성과 테스트 격리는 아직 약하다. 가장 큰 개선 효과는 패키지 이동보다 포트 입출력 타입을 애플리케이션 소유 타입으로 바꾸는 데서 나온다.
