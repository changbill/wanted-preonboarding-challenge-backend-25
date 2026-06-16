# [fix] PG 선택 문자열 변환과 예외 응답 추가

## 작업 개요

`PLAN.md`의 `2단계: PG adapter 선택 방식 개선` 미완료 todo를 완료했습니다.

## 작업 내용

- `PgCorp.from(String)`으로 `pgCorpName` 문자열 변환 경로 통합
- `PaymentRequest`가 문자열 대신 `PgCorp`를 보관하도록 변경
- 지원하지 않는 PG 이름 또는 adapter 없는 PG 요청을 `UnsupportedPgCorpException`으로 명시
- `GlobalException`에서 미지원 PG 요청을 HTTP 400 `ErrorResponse`로 응답
- phase 기준을 `PLAN.md` 단계 단위로 고정하는 하네스 설정 추가
- phase 산출물과 `PLAN.md`, `RESEARCH.md`, `SPEC.md` 갱신

## 변경 이유

기존에는 `pgCorpName` 문자열을 여러 위치에서 `valueOf()`로 직접 변환해 대소문자, 하이픈 표기, 미지원 PG 예외 처리가 일관되지 않았습니다.

PG 식별자 변환을 `PgCorp.from()`으로 모으고 adapter 선택은 `EnumMap<PgCorp, ...>` 기준으로 유지해 신규 PG 추가 시 계약을 명확히 했습니다.

## 테스트 내용

- [x] `.\gradlew.bat compileTestJava`
- [x] `.\gradlew.bat test`
- [x] `PgCorp.from()` 문자열 파싱 테스트
- [x] 미지원 PG adapter/widget 예외 테스트

## 영향 범위

- 결제 위젯 PG 선택
- 결제 승인 PG 선택
- 미지원 PG 요청의 웹 예외 응답
- Codex phase 기준과 산출물 정책

## 리뷰 포인트

- `PgCorp.from()`의 입력 정규화 범위가 현재 API 요구사항에 충분한지 확인
- `UnsupportedPgCorpException`을 HTTP 400으로 처리하는 정책이 적절한지 확인

## 참고 사항

`gh` CLI가 설치되어 있지 않아 자동 PR 생성 대신 이 초안을 남겼습니다.

수동 PR 생성 URL:
https://github.com/changbill/wanted-preonboarding-challenge-backend-25/pull/new/feature/2-PG-adapter-%EC%84%A0%ED%83%9D-%EB%B0%A9%EC%8B%9D-%EA%B0%9C%EC%84%A0
