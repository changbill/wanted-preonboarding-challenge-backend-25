---
name: codex-orchestrator
description: `/run {phase}` 요청을 받아 기획, 구현, 검증, 수정, 커밋까지 수행한다.
---

# Codex Orchestrator

## When to Use

- 사용자가 `/run {phase}` 또는 `/phase {phase}`를 요청할 때 사용한다.
- 산출물, 검증, 커밋까지 필요한 작업에 사용한다.

## Workflow

1. `guard.ps1 -Command run -Phase {phase} -Stage start`
2. `feature/...` 브랜치 확인. `master` 또는 `main`이면 `feature/{번호}-{작업요약}` 브랜치 생성/전환
3. phase 번호를 `PLAN.md` 단계 번호에 매핑한다. `phase-02`는 `2단계` 전체다.
4. 루트 `PLAN.md`의 해당 단계 todo를 기준으로 범위를 잡는다.
5. 서브 에이전트는 큰 조사나 리뷰가 필요할 때만 사용한다.
6. 구현 후 `_workspace/codex/{phase}/implementation.md` 작성
7. `.\gradlew.bat test` 실행
8. 실패하면 범위 안에서 수정하고 다시 검증. 같은 실패가 3회 반복되면 `blocked`
9. 통과하면 `_workspace/codex/{phase}/verification.md`에 `gradlew.bat test: PASS` 기록
10. 필요한 문서만 읽고 수정한다.
    - 동작 정의 변경: `SPEC.md`
    - 조사/판단 변경: `RESEARCH.md`
    - 실행/입문 변경: `README.md`
    - 계획 체크 변경: `PLAN.md`
11. 코드, 문서, phase 산출물, Codex/하네스 설정 파일 stage
12. 한글 커밋 메시지로 commit
13. `guard.ps1 -Command run -Phase {phase} -Stage finish`
14. feature 브랜치를 push하고 `gh pr create --base master --head {branch}`로 PR 생성

## Required Artifacts

- `_workspace/codex/{phase}/implementation.md`
- `_workspace/codex/{phase}/verification.md`

## PR Policy

- phase 완료 후 현재 `feature/...` 브랜치를 `master`로 병합하기 위한 PR을 생성한다.
- 기본 명령은 `gh pr create --base master --head {branch}`다.
- PR 제목과 본문은 저장소 PR 작성 규칙을 따른다.
- PR 생성 실패 시 최종 보고에 원인과 다음 명령을 남긴다.

## Commit Includes

- 검증된 코드/테스트/문서 변경
- required artifacts
- `AGENTS.md`
- `.codex/config.toml`
- `.agents/skills/codex-orchestrator/SKILL.md`
- `.agents/harness/guard.ps1`
- `.githooks/pre-commit`

## Failure Policy

- 구현/커밋 브랜치는 `feature/...`를 사용한다.
- 커밋 전 테스트 통과를 확인한다.
- 계획 밖 변경이 필요하면 멈추고 보고한다.
- 최종 보고는 변경, 검증, 커밋/PR만 짧게 남긴다.
