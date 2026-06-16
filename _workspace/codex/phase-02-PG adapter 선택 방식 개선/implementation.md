# Implementation

## 변경 내용

- `PaymentAPIs.provider()` 계약 추가
- `PgWidget.provider()` 계약 추가
- `TossPayment`, `TossWidget`이 `PgCorp.TOSS`를 반환하도록 구현
- `PaymentService`의 PG adapter map을 `EnumMap<PgCorp, PaymentAPIs>`로 변경
- `PaymentService`의 요청별 mutable field `paymentAPIs` 제거
- `PgWidgetService`의 클래스명 `split("Widget")` 기반 selector 제거
- PG adapter 선택 단위 테스트 추가
- PG widget 선택 단위 테스트 추가

## 변경 이유

클래스명에서 문자열을 잘라 PG 식별자로 쓰면 클래스명 변경이 런타임 동작을 깨뜨린다. adapter가 지원하는 PG를 명시적으로 제공하고 서비스가 enum key로 선택하도록 바꿔 신규 PG adapter 추가 시 계약이 분명해지도록 했다.
