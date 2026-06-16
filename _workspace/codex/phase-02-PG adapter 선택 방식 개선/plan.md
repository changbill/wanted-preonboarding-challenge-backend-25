# Plan

## 목표

PG adapter 선택을 클래스명 문자열 규칙에서 명시적 provider 계약으로 변경한다.

## 범위

- `PaymentAPIs`와 `PgWidget`에 `provider()` 계약 추가
- `TossPayment`, `TossWidget`이 `PgCorp.TOSS` 반환
- `PaymentService`의 `Map<String, PaymentAPIs>`를 `EnumMap<PgCorp, PaymentAPIs>`로 변경
- `PaymentService.paymentAPIs` 요청별 mutable field 제거
- `PgWidgetService`의 클래스명 split 기반 selector 제거
- `PaymentService.selectPgAPI(PgCorp)` 단위 테스트 추가

## 제외

- 결제/취소 상태 전이 변경
- DB schema 변경
- 신규 PG adapter 구현
- Toss API 호출 로직 변경

## 검증

- `.\gradlew.bat compileTestJava`
- `.\gradlew.bat test`
