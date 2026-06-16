# Codex 전용 에이전트 가이드

이 저장소의 작업은 `/run {phase}` 하나로 진행한다. 오케스트레이터가 기획, 구현, 검증, 수정, 커밋까지 이어서 수행한다.

## 규칙

- 작업은 항상 `feature/...` 브랜치에서 수행한다. `master` 또는 `main`에서 구현/커밋하지 않는다.
- 현재 브랜치가 `master` 또는 `main`이면 `/run` 시작 직후 `feature/{번호}-{작업요약}` 브랜치를 만들고 전환한다.
- 계획, 구현 요약, 검증 결과는 `_workspace/codex/{phase}/`에 남긴다.
- 커밋에는 코드 변경, phase 산출물, Codex/하네스 설정을 함께 포함한다.
- phase 완료 후 feature 브랜치를 push하고 `master` 대상 PR 생성을 시도한다. PR 생성 도구나 인증이 없으면 `_workspace/codex/{phase}/pr.md`에 PR 제목/본문을 남기고 보고한다.
- 사용자 결정이 필요한 범위 변경, 위험한 작업, 반복 검증 실패가 아니면 중간 승인을 요구하지 않는다.

## 명령

| 명령 | 목적 |
|------|------|
| `/run {phase}` | phase 전체 수행 |
| `/phase {phase}` | `/run`과 동일 |
| `/status` | 현재 상태 확인 |

## 필수 검증

```powershell
.\gradlew.bat test
```

## 핵심 파일

- `.agents/skills/codex-orchestrator/SKILL.md`
- `.agents/harness/guard.ps1`
- `.agents/harness/set-state.ps1`
- `.githooks/pre-commit`
- `_workspace/codex/state.json`
