# Request

## User Command

`/run phase-01-test-import-fix`

## Goal

오케스트레이터가 phase 전체를 지휘해 `.\gradlew.bat test` 실패를 해결하고, 가능한 경우 검증 통과 후 커밋까지 완료한다.

## Success Criteria

- 테스트 실행 실패 원인을 조사한다.
- 계획 범위 안에서 테스트 설정 또는 테스트 코드를 수정한다.
- `.\gradlew.bat test`가 통과한다.
- phase 산출물(`plan.md`, `implementation.md`, `verification.md`, `review.md`, `commit.md`, `final.md`)을 남긴다.
- 검증된 변경만 커밋한다.
