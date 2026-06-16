# Verification

```text
gradlew.bat compileTestJava: PASS
gradlew.bat test: PASS
```

## 결과

- 테스트 컴파일 통과
- 전체 테스트 통과
- `PgCorp.from()` 문자열 파싱 테스트 통과
- 지원하지 않는 PG adapter/widget 선택 예외 테스트 통과

## 참고

- Gradle deprecated feature 경고는 남아 있으나 이번 phase 범위 밖이다.
