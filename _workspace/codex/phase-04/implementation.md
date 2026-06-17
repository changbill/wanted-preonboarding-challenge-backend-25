# phase-04 implementation

## 작업 개요

JPA 엔티티와 핵심 도메인 모델을 분리했다. 주문, 주문상품, 결제 원장, 카드 결제 모델에서 JPA 어노테이션과 persistence import를 제거하고, MySQL persistence 전용 JPA 엔티티와 mapper를 추가했다.

## 변경 내용

- `Order`, `OrderItem`, `PurchaseOrderId`, `PaymentLedger`, `CardPayment`, `TransactionType`을 persistence 어노테이션이 없는 도메인 모델로 정리
- `infrastructure.persistence.mysql.entity.order`에 `PurchaseOrderJpaEntity`, `OrderItemJpaEntity`, `PurchaseOrderJpaId` 추가
- `infrastructure.persistence.mysql.entity.payment`에 `PaymentLedgerJpaEntity`, `CardPaymentJpaEntity`, `TransactionTypeJpaEntity` 추가
- JPA converter를 `infrastructure.persistence.mysql.entity.converter`로 이동
- `OrderPersistenceMapper`, `PaymentPersistenceMapper` 추가
- Spring Data JPA repository가 persistence entity를 다루도록 변경
- repository adapter에서 persistence entity와 domain model 변환 적용
- JPA repository 테스트를 persistence entity 기준으로 수정
- 도메인 JPA 의존 금지 ArchUnit 테스트와 mapper 왕복 테스트 추가

## 범위 밖

- `PaymentSettlements`는 phase-04 핵심 대상이 아니어서 domain 패키지에 남겨 두었다.
- `domain.entity` 패키지명 전체를 `domain.model`로 재배치하지는 않았다.
