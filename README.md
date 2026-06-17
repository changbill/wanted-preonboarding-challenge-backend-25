# wanted-preonboarding-challenge-backend-25

Spring Boot 기반 PG 통합 결제 예제다. 현재 PG adapter는 Toss 중심이다.

## 환경

- Java 21
- Spring Boot 3.2.4
- MySQL 8.0.33 또는 H2 test runtime
- Gradle Wrapper

## 실행

```powershell
.\gradlew.bat test
```

애플리케이션 실행 전 MySQL이 필요하면 `docker-compose.yml`을 사용한다.

### Toss 설정

Toss API 설정은 환경변수로 주입할 수 있다. 값을 주입하지 않으면 `application.yml`의 기본값을 사용한다.

- `TOSS_PAYMENTS_BASE_URL`
- `TOSS_PAYMENTS_SECRET_KEY`
- `TOSS_PAYMENTS_CONNECT_TIMEOUT_SECONDS`
- `TOSS_PAYMENTS_WRITE_TIMEOUT_SECONDS`
- `TOSS_PAYMENTS_READ_TIMEOUT_SECONDS`
- `TOSS_PAYMENTS_RETRY_ON_CONNECTION_FAILURE`

## 구조

```text
src/main/java/com/wanted/clone/oneport
  core       공통 응답, 예외, 설정
  member     gRPC 회원 예제
  payments   주문, 결제, PG adapter, persistence
```

## 주요 문서

- `PLAN.md`: 단계별 개선 계획과 완료 상태
- `SPEC.md`: 현재 구현된 동작
- `RESEARCH.md`: 선택 근거와 남은 리스크
- `AGENTS.md`: Codex 하네스 규칙

## 작업 규칙

- 기능 변경은 `feature/...` 브랜치에서 수행한다.
- phase 기준은 `PLAN.md`의 단계다.
- phase별 산출물은 `_workspace/codex/{phase}/implementation.md`, `verification.md`만 남긴다.
- PR은 `gh pr create --base master --head {branch}`로 생성한다.
