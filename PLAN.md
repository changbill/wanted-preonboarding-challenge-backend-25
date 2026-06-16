# 개선 계획

## 1단계: 아키텍처 경계 정리

- [x] `presentation.port.in`을 `application.port.in`으로 이동
- [x] 입력 포트 메서드가 웹 DTO 대신 application command/result 타입을 사용하도록 변경
- [x] 출력 포트 `PaymentAPIs`가 Toss DTO 대신 application result 타입을 반환하도록 변경
- [x] `PgCorp`를 presentation 패키지 밖으로 이동
- [x] 도메인에서 Toss response 타입 참조 제거

## 2단계: PG adapter 선택 방식 개선

- [ ] PG 식별자 enum 또는 value object 정의
- [x] `PaymentAPIs.provider()` 계약 추가
- [x] `PgWidget.provider()` 계약 추가
- [x] `TossPayment.provider()`에서 `PgCorp.TOSS` 반환
- [x] `TossWidget.provider()`에서 `PgCorp.TOSS` 반환
- [x] 신규 PG adapter 추가 시 각 adapter가 자기 `PgCorp` 반환
- [x] `PaymentService` PG selector를 `EnumMap<PgCorp, PaymentAPIs>`로 변경
- [x] `PgWidgetService` PG selector를 `EnumMap<PgCorp, PgWidget>`로 변경
- [x] 클래스명 `split()` 기반 selector 제거
- [ ] 문자열 `pgCorpName`을 `PgProvider.from()`으로 변환
- [ ] 지원하지 않는 PG 요청의 예외 응답 정의
- [x] `selectPgAPI(PgCorp)` 단위 테스트 추가
- [x] `PgWidgetService` provider 선택 단위 테스트 추가

## 3단계: 결제/취소 유스케이스 안정화

- [x] `PaymentService` 요청별 mutable field 제거
- [ ] 결제 승인 중복 처리 규칙 정의
- [ ] 동일 `orderId` 결제 승인 요청 idempotent 처리
- [ ] 동일 `paymentKey` 결제 원장 중복 저장 방지 규칙 정의
- [ ] 주문 상태 전이 규칙을 도메인 메서드로 이동
- [ ] 전체 취소와 부분 취소 상태 전이 구현
- [ ] 취소 가능 금액 검증 실패 예외 정의

## 4단계: JPA 엔티티와 도메인 모델 분리

- [ ] `domain.entity` 패키지명을 `domain.model` 또는 `domain` 중심으로 재정리
- [ ] `Order`, `OrderItem`, `PaymentLedger`, `CardPayment` 순수 도메인 모델 정의
- [ ] `@Entity`, `@Table`, `@Column`, `@Convert` 등 JPA 어노테이션을 persistence entity로 이동
- [ ] `infrastructure.persistence.mysql.entity` 패키지에 JPA entity 정의
- [ ] 도메인 모델과 JPA entity 간 mapper 추가
- [ ] repository adapter에서 JPA entity 조회 후 도메인 모델로 변환
- [ ] repository adapter에서 도메인 모델 저장 전 JPA entity로 변환
- [ ] 도메인 모델이 JPA, Spring, Toss, web 타입을 참조하지 않도록 정리
- [ ] JPA entity는 상태 전이 규칙 없이 persistence 매핑 책임만 가지도록 정리
- [ ] 분리 후 도메인 상태 전이 단위 테스트 추가

## 5단계: 결제/취소 락 적용

- [ ] `OrderRepository` lock 조회 포트 추가
- [ ] `JpaOrderRepository.findByIdForUpdate` 비관적 쓰기 락 추가
- [ ] 결제 승인 유스케이스에서 주문 row lock 적용
- [ ] 결제 취소 유스케이스에서 주문 row lock 적용
- [ ] 외부 PG API 호출 전 `PAYMENT_APPROVING` 진행 상태 선점 방식 검토
- [ ] lock wait timeout/deadlock 예외 처리 정책 정의
- [ ] 결제 원장 unique constraint 추가

## 6단계: 설정과 외부 연동 정리

- [ ] Toss base URL, secret key, timeout을 `@ConfigurationProperties`로 이동
- [ ] secret 값을 환경변수 또는 profile별 설정으로 주입
- [ ] Retrofit error body를 공통 PG 오류로 변환
- [ ] 외부 API timeout/retry 정책 결정

## 7단계: DB schema와 JPA 매핑 정합성 정리

- [ ] `purchase_order.order_id` 타입과 `order_items.order_id` 타입 정합성 확인
- [ ] `OrderItem` 복합키와 `create_schema.sql` PK 정의 정리
- [ ] `card_payment` entity table name과 SQL table name 정합성 확인
- [ ] `ddl-auto` 전략과 init SQL 사용 방식을 분리

## 8단계: 테스트와 아키텍처 검증 보강

- [ ] 결제 승인 application service 단위 테스트 추가
- [ ] 결제 취소 application service 단위 테스트 추가
- [ ] 동일 주문 결제 승인 동시성 테스트 추가
- [ ] 승인/취소 경합 동시성 테스트 추가
- [ ] `findByIdForUpdate` lock 대기 테스트 추가
- [ ] Toss adapter MockWebServer 테스트 추가
- [ ] 상태 전이 도메인 테스트 추가
- [ ] ArchUnit 의존 규칙 강화
- [ ] REST Docs/OpenAPI 생성 테스트 정리
