# phase-05 구현 기록

## 작업 개요

결제 승인과 결제 취소가 같은 주문에 동시에 접근할 때 상태 전이가 겹치지 않도록 주문 row 기준 비관적 쓰기 lock을 적용했다.

## 변경 내용

- `OrderRepository.findByIdForUpdate(String)` 포트를 추가했다.
- `JpaOrderRepository.findByIdForUpdate`에 `@Lock(PESSIMISTIC_WRITE)`와 JPQL 조회를 추가했다.
- `PaymentService.paymentApproved`가 주문을 lock 조회하고 승인 성공 후 주문 상태를 저장하도록 변경했다.
- `CancelService.orderCancel`이 주문을 lock 조회하고 취소 성공 후 주문 상태를 저장하도록 변경했다.
- lock wait timeout/deadlock 계열 예외를 HTTP 409 `ErrorResponse`로 반환하도록 `GlobalException`에 핸들러를 추가했다.
- `payment_ledger` JPA entity와 schema에 `tx_id`, `method`, `payment_status` unique constraint를 추가했다.
- 승인/취소 서비스 단위 테스트에서 lock 조회와 주문 저장을 검증했다.

## 판단 기록

- 현재 주문 상태 enum에는 `PAYMENT_APPROVING` 진행 상태가 없다. 새 상태 추가는 상태 전이와 외부 응답 의미가 커지는 변경이라 phase-05에서는 보류했다.
- 외부 PG API 호출은 현재 트랜잭션 내부에 남아 있다. 이 구조는 주문 row lock 보유 시간이 PG 응답 시간에 영향을 받지만, 별도 진행 상태 선점 모델이 없는 현재 코드에서는 같은 주문의 승인/취소 경합을 가장 작게 막는 방식이다.

## 변경 파일

- `src/main/java/com/wanted/clone/oneport/payments/application/port/out/repository/OrderRepository.java`
- `src/main/java/com/wanted/clone/oneport/payments/infrastructure/persistence/mysql/order/JpaOrderRepository.java`
- `src/main/java/com/wanted/clone/oneport/payments/infrastructure/persistence/mysql/order/OrderRepositoryImpl.java`
- `src/main/java/com/wanted/clone/oneport/payments/application/service/PaymentService.java`
- `src/main/java/com/wanted/clone/oneport/payments/application/service/CancelService.java`
- `src/main/java/com/wanted/clone/oneport/payments/infrastructure/persistence/mysql/entity/payment/PaymentLedgerJpaEntity.java`
- `src/main/resources/initdb/create_schema.sql`
- `src/main/java/com/wanted/clone/oneport/core/common/GlobalException.java`
- `src/test/java/com/wanted/clone/oneport/payments/application/service/PaymentServiceTests.java`
- `src/test/java/com/wanted/clone/oneport/payments/application/service/CancelServiceTests.java`
