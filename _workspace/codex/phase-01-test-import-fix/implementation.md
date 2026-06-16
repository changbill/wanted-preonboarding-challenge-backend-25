# Implementation

## 변경 파일

- `src/main/resources/application.yml`
- `src/test/java/com/wanted/clone/oneport/OnePortApplicationTests.java`
- `src/test/java/com/wanted/clone/oneport/payments/presentation/web/OrderControllerDocTest.java`

## 변경 내용

- `test` profile에 H2 in-memory datasource를 추가했다.
- `test` profile에 `org.hibernate.dialect.H2Dialect`를 명시했다.
- `OnePortApplicationTests`가 `dev` 대신 `test` profile로 실행되도록 바꿨다.
- `OrderControllerDocTest`가 `dev` 대신 `test` profile로 실행되도록 바꿨다.

## 변경 이유

기존 context 기반 테스트는 `dev` profile을 사용해 로컬 MySQL `localhost:13306`에 의존했다. 로컬 DB가 없으면 Hibernate가 JDBC metadata를 읽지 못하고 dialect 결정에 실패한다. 테스트는 외부 DB 없이 재현 가능해야 하므로 H2 기반 `test` profile로 분리했다.
