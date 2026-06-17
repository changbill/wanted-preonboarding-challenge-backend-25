# Implementation

## 변경 내용

- `PaymentRuleViolationException` 추가
- `PaymentService.paymentApproved()`에 결제 승인 idempotency 규칙 추가
- 이미 `DONE` 원장이 있는 `paymentKey`를 PG 호출 전에 거부
- `Order.orderPaymentFullFill()`, `orderAllCancel()`, `orderCancel()`에 상태 전이 검증 추가
- `ORDER_PARTIAL_CANCELLED` 상태 추가
- 부분 취소 시 선택 상품만 취소하고 전체 상품 취소 시 주문 전체 취소 처리
- `PaymentLedger.verifyCancellableAmount()` 추가
- `PaymentCancelResult.toEntity(PgCorp)`로 취소 원장에도 PG 정보를 저장
- `GlobalException`에서 결제 규칙 위반을 HTTP 400으로 응답
- 결제 승인 중복, 도메인 취소 상태 전이, 취소 가능 금액 테스트 추가

## 구현 규칙

- 동일 주문과 동일 `paymentKey` 승인 재요청은 성공으로 반환한다.
- 이미 결제된 주문의 다른 `paymentKey` 승인 요청은 거부한다.
- 동일 `paymentKey`의 승인 원장이 이미 있으면 PG 승인 API를 호출하지 않는다.
- 취소 가능 금액 초과, 구매 확정 주문 취소, 없는 주문 상품 취소는 결제 규칙 위반으로 처리한다.
