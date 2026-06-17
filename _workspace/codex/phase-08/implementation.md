# phase-08 구현 요약

## 변경

- 결제 승인 service 단위 테스트에 PG 미승인 실패 반환과 lock 실패 전파 케이스를 추가했다.
- 결제 취소 service 단위 테스트에 취소 가능 금액 실패와 lock 실패 전파 케이스를 추가했다.
- 주문 도메인 테스트에 payment id 일치성 검증을 추가했다.
- `JpaOrderRepository.findByIdForUpdate`의 `PESSIMISTIC_WRITE` lock annotation 테스트를 추가했다.
- Toss adapter MockWebServer 테스트에 승인 성공, 취소 성공 응답 변환 검증을 추가했다.
- ArchUnit이 main class만 검사하도록 정리하고 application/domain 의존 방향 규칙을 추가했다.
- REST Docs/OpenAPI snippet 이름을 `order-new-success`, `order-new-validation-error`로 정리했다.
- `PLAN.md`, `SPEC.md`, `RESEARCH.md`를 phase-08 결과에 맞게 갱신했다.

## 판단

- 실제 MySQL lock wait/deadlock 재현은 H2 단위 테스트에서 안정적으로 검증하지 않는다.
- 이번 phase에서는 lock 사용 계약과 lock 실패 전파를 테스트로 고정했다.
- `PaymentSettlements`는 아직 domain 패키지의 JPA entity 제한사항이므로 ArchUnit 순수 domain 규칙에서 제외했다.
