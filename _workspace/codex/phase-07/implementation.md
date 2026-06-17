# phase-07 구현 요약

## 변경

- `create_schema.sql`의 `order_items.order_id`를 JPA `String` FK와 맞게 `VARCHAR(255)`로 정리했다.
- `order_items` 기본키를 JPA embedded id 기준인 `(order_id, item_idx)`로 정리했다.
- `order_items`에 `purchase_order(order_id)` 외래키를 추가했다.
- 카드 결제 SQL table을 `card_payment_ledger`에서 JPA entity 이름과 같은 `card_payment`로 맞췄다.
- 카드 결제 기본키 column을 `tx_id`에서 `payment_key`로 맞췄다.
- dev profile은 Docker init SQL schema를 사용하고 JPA는 `ddl-auto=validate`로 검증하도록 분리했다.
- test profile은 H2 테스트 격리를 위해 `ddl-auto=create-drop`을 유지했다.
- schema 핵심 정합성을 확인하는 단위 테스트를 추가했다.
- `PLAN.md`, `RESEARCH.md`, `SPEC.md`를 실제 구현 상태에 맞게 갱신했다.

## 판단

- 현재 phase 범위는 schema와 JPA 매핑 정합성이다.
- `payment_settlements`는 아직 도메인/JPA 분리 대상이므로 별도 제한사항으로 남겼다.
