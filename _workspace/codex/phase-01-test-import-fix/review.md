# Review

## Reviewed Diff

- `src/main/resources/application.yml`
- `src/test/java/com/wanted/clone/oneport/OnePortApplicationTests.java`
- `src/test/java/com/wanted/clone/oneport/payments/presentation/web/OrderControllerDocTest.java`

## 판단

PASS.

변경은 테스트 실행 안정화 범위에 한정되어 있다. `dev` profile의 MySQL 설정은 유지했고, `test` profile에만 H2 datasource와 dialect를 추가했다. SpringBootTest 두 곳은 외부 MySQL에 의존하지 않도록 `test` profile로 전환했다.

## 검증 근거

- `gradlew.bat compileTestJava: PASS`
- `gradlew.bat test: PASS`

## 하위 에이전트 검토

explorer가 독립적으로 같은 원인을 확인했다.

- `dev` profile이 로컬 MySQL에 의존함
- `test` profile datasource가 필요함
- `PaymentLedgerRepositoryTests`는 `@DataJpaTest`의 embedded datasource 대체로 직접 원인이 아님

## 남은 위험

- Gradle deprecated feature 경고는 남아 있으나 이번 phase 범위 밖이다.
- H2와 MySQL 간 SQL dialect 차이는 존재하지만, 이 phase의 목표는 로컬 테스트 실행 안정화다.
