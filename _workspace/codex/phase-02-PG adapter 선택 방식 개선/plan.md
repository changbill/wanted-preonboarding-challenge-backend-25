# Plan

## 목표

`PLAN.md`의 `2단계: PG adapter 선택 방식 개선` todo 전체를 완료한다. PG adapter 선택을 클래스명 문자열 규칙에서 명시적 provider 계약과 PG 식별자 파싱 계약으로 변경한다.

## 범위

- `PaymentAPIs`와 `PgWidget`에 `provider()` 계약 추가
- `TossPayment`, `TossWidget`이 `PgCorp.TOSS` 반환
- `PaymentService`의 `Map<String, PaymentAPIs>`를 `EnumMap<PgCorp, PaymentAPIs>`로 변경
- `PaymentService.paymentAPIs` 요청별 mutable field 제거
- `PgWidgetService`의 클래스명 split 기반 selector 제거
- `PaymentService.selectPgAPI(PgCorp)` 단위 테스트 추가
- `PgCorp.from(String)`으로 `pgCorpName` 문자열 파싱
- 지원하지 않는 PG 요청의 예외와 HTTP 400 응답 정의

## 서브 에이전트 판단

- 사용하지 않음
- 이유: 남은 작업이 `PgCorp` 파싱, adapter 선택, 예외 응답으로 같은 호출 흐름에 묶여 있어 병렬 분리보다 단일 컨텍스트에서 수정하는 편이 정확함

## 제외

- 결제/취소 상태 전이 변경
- DB schema 변경
- 신규 PG adapter 구현
- Toss API 호출 로직 변경

## 검증

- `.\gradlew.bat compileTestJava`
- `.\gradlew.bat test`
