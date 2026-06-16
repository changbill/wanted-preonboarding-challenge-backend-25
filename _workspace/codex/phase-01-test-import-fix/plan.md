# phase-01-test-import-fix Plan

## 목표

`.\gradlew.bat test`가 로컬 외부 MySQL 없이 통과하도록 Spring 테스트 profile과 datasource 설정을 안정화한다.

## 조사 요약

- `.\gradlew.bat compileTestJava`: 통과 확인
- `.\gradlew.bat test`: Spring context 부팅 실패
- 실패 테스트:
  - `OnePortApplicationTests > contextLoads`
  - `OrderControllerDocTest > newOrder_2XX_CorrectConstraintValue`
  - `OrderControllerDocTest > newOrder_4XX_ConstraintHasValueBlank`
- 핵심 오류:
  - `Unable to determine Dialect without JDBC metadata`
- 원인:
  - `OnePortApplicationTests`와 `OrderControllerDocTest`가 `@ActiveProfiles("dev")`를 사용한다.
  - `dev` profile datasource는 `localhost:13306` MySQL에 의존한다.
  - `test` profile에는 JPA 설정만 있고 datasource URL이 없다.

## In Scope

- 테스트가 `test` profile을 사용하도록 변경
- `test` profile에 H2 datasource와 Hibernate dialect 설정 추가
- 필요 시 REST Docs 테스트가 H2에서 부팅되도록 최소 테스트 설정 보정
- phase 산출물 작성
- `.\gradlew.bat test` 통과 검증

## Out of Scope

- production/dev datasource 의미 변경
- 결제 도메인 로직 변경
- DB schema/JPA 매핑 전면 개편
- Toss API 연동 방식 변경
- 테스트 커버리지 확대

## 예상 수정 파일

- `src/main/resources/application.yml`
- `src/test/java/com/wanted/clone/oneport/OnePortApplicationTests.java`
- `src/test/java/com/wanted/clone/oneport/payments/presentation/web/OrderControllerDocTest.java`

## 구현 Todo

- [ ] `test` profile에 H2 datasource 설정 추가
- [ ] context 부팅 테스트 profile을 `test`로 변경
- [ ] REST Docs 컨트롤러 테스트 profile을 `test`로 변경
- [ ] `.\gradlew.bat test` 실행
- [ ] 실패가 남으면 같은 phase 범위 안에서 fix loop 수행
- [ ] diff 리뷰 후 검증된 변경만 커밋

## 검증 계획

- `.\gradlew.bat compileTestJava`
- `.\gradlew.bat test`

## 커밋 기준

- `verification.md`에 `gradlew.bat test: PASS` 기록
- 테스트 안정화 변경과 phase 산출물 기록 완료
- 검증 통과 후 한글 커밋 메시지로 커밋

## 위험

- H2에서 MySQL과 다른 예약어/DDL 동작이 드러날 수 있다.
- context 부팅 이후 컨트롤러 응답 또는 REST Docs snippet 생성 실패가 추가로 나타날 수 있다.
