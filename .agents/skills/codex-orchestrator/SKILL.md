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
3. 조사 후 `_workspace/codex/{phase}/plan.md` 작성
4. 서브 에이전트 사용 여부를 판단한다. 병렬 조사/리뷰/분리 구현 이득이 있으면 위임하고, 이득이 작으면 직접 수행한다.
5. 계획 범위 구현 후 `_workspace/codex/{phase}/implementation.md` 작성
6. `.\gradlew.bat test` 실행
7. 실패하면 계획 범위 안에서 수정하고 다시 검증. 같은 실패가 3회 반복되면 `blocked`
8. 통과하면 `_workspace/codex/{phase}/verification.md`에 `gradlew.bat test: PASS` 기록
9. 구현 결과가 `PLAN.md`, `README.md`, `RESEARCH.md`, `SPEC.md`와 달라지는지 확인하고 필요한 문서만 갱신
10. 코드 변경, 문서 변경, phase 산출물, Codex/하네스 설정 파일을 함께 stage
11. 한글 커밋 메시지로 commit
12. `guard.ps1 -Command run -Phase {phase} -Stage finish`
13. feature 브랜치를 remote에 push하고 `master` 대상 PR 생성을 시도한다. `gh pr create` 사용이 불가능하거나 인증이 없으면 `_workspace/codex/{phase}/pr.md`에 PR 제목/본문을 남기고 최종 보고에 PR 미생성 사유를 기록한다.

## Required Artifacts

- `_workspace/codex/{phase}/plan.md`
- `_workspace/codex/{phase}/implementation.md`
- `_workspace/codex/{phase}/verification.md`
- `_workspace/codex/state.json`

## PR Policy

- phase 완료 후 현재 `feature/...` 브랜치를 `master`로 병합하기 위한 PR을 생성한다.
- 기본 명령은 `gh pr create --base master --head {branch}`다.
- PR 제목과 본문은 저장소 PR 작성 규칙을 따른다.
- 네트워크, 인증, 원격 저장소 설정, `gh` 미설치로 PR 생성이 불가능하면 커밋은 유지하고 `_workspace/codex/{phase}/pr.md`에 PR 초안을 남긴다.
- PR 생성 실패는 구현 검증 실패로 취급하지 않지만, 최종 보고에 원인과 다음 수동 명령을 명확히 남긴다.

## Commit Includes

- 검증된 코드/테스트/문서 변경
- required artifacts
- `AGENTS.md`
- `.codex/config.toml`
- `.agents/skills/codex-orchestrator/SKILL.md`
- `.agents/harness/guard.ps1`
- `.agents/harness/set-state.ps1`
- `.githooks/pre-commit`

## Failure Policy

- `feature/...` 브랜치가 아니면 구현/커밋하지 않는다.
- 테스트 실패를 숨기고 커밋하지 않는다.
- 계획 밖 변경이 필요하면 멈추고 보고한다.
- 서브 에이전트를 쓰지 않은 경우에는 산출물 또는 최종 보고에 이유를 남긴다.
