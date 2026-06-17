# phase-06 구현 요약

## 변경

- Toss API base URL, secret key, timeout, OkHttp 연결 실패 retry 설정을 `TossPaymentProperties`로 외부화했다.
- `application.yml`에서 Toss 설정을 공통 기본값으로 두고 환경변수 주입을 지원했다.
- Retrofit 실패 응답 body의 `{code, message}`를 `PgPaymentGatewayException`으로 변환했다.
- 전역 예외 처리에서 PG gateway 오류를 HTTP 502 `ErrorResponse`로 반환하도록 추가했다.
- Toss error body 변환 단위 테스트를 추가했다.
- `PLAN.md`, `README.md`, `RESEARCH.md`, `SPEC.md`를 실제 구현 상태에 맞게 갱신했다.

## 판단

- 승인/취소 API는 중복 요청 부작용이 있으므로 애플리케이션 레벨 자동 retry는 넣지 않았다.
- 네트워크 연결 실패 재시도는 OkHttp `retryOnConnectionFailure` 설정으로만 제어한다.
