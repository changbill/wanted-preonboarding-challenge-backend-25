# Codex 전용 에이전트 가이드

이 저장소의 작업은 `/run {phase}` 하나로 진행한다. 오케스트레이터가 구현, 검증, 커밋, PR까지 수행한다.

## 규칙

- 작업 브랜치는 항상 `feature/...`를 사용한다.
- 현재 브랜치가 `master` 또는 `main`이면 `/run` 시작 직후 `feature/{번호}-{작업요약}` 브랜치를 만들고 전환한다.
- phase 기준은 `PLAN.md`의 단계다. 예를 들어 `phase-02`는 `PLAN.md`의 `2단계` todo 전체를 완료하는 단위다.
- 계획은 루트 `PLAN.md`로 관리한다.
- 구현 요약과 검증 결과는 `_workspace/codex/{phase}/implementation.md`, `verification.md`에 남긴다.
- 커밋에는 코드 변경, phase 산출물, Codex/하네스 설정을 함께 포함한다.
- phase 완료 후 feature 브랜치를 push하고 `master` 대상 PR을 생성한다.
- 문서는 조건부로만 읽고 수정한다. 동작 정의는 `SPEC.md`, 조사/판단은 `RESEARCH.md`, 실행/입문은 `README.md`, 계획 체크는 `PLAN.md`다.
- 검증은 기본적으로 `.\gradlew.bat test`만 실행한다.
- 서브 에이전트는 큰 조사나 리뷰에만 사용한다.
- 최종 보고는 변경, 검증, 커밋/PR만 짧게 남긴다.
- 사용자 결정이 필요한 범위 변경, 위험한 작업, 반복 검증 실패에서는 보고 후 진행한다.

## 명령

| 명령 | 목적 |
|------|------|
| `/run {phase}` | phase 전체 수행 |
| `/phase {phase}` | `/run`과 동일 |

## 필수 검증

```powershell
.\gradlew.bat test
```

## 핵심 파일

- `.agents/skills/codex-orchestrator/SKILL.md`
- `.agents/harness/guard.ps1`
- `.githooks/pre-commit`
