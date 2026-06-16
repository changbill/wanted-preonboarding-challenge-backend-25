# 조사 기록

## 기준 자료

- Hexagonal Architecture: https://alistair.cockburn.us/hexagonal-architecture/
- ArchUnit User Guide: https://www.archunit.org/userguide/html/000_Index.html
- Spring Modulith Reference: https://docs.spring.io/spring-modulith/reference/fundamentals.html

## 현재 판단

- 결제 도메인은 `presentation`, `application`, `domain`, `infrastructure`로 나뉘어 있다.
- 포트는 애플리케이션 소유 타입을 사용해야 한다. 웹 DTO와 Toss DTO는 adapter 경계 밖으로 새지 않아야 한다.
- PG 선택은 클래스명 규칙이 아니라 `PaymentAPIs.provider()`, `PgWidget.provider()`가 반환하는 `PgCorp`로 한다.
- 외부 입력의 `pgCorpName`은 `PgCorp.from(String)`에서 정규화한다.
- 지원하지 않는 PG는 `UnsupportedPgCorpException`으로 거부하고 웹에서는 HTTP 400으로 응답한다.
- singleton service에 요청별 PG adapter 상태를 필드로 저장하지 않는다.

## 남은 리스크

- 결제 승인/취소 idempotency가 없다.
- 동일 주문 결제/취소 동시성 제어가 없다.
- Toss secret과 base URL이 설정/코드에 고정되어 있다.
- JPA entity와 `create_schema.sql`의 ID/PK 구조가 다를 수 있다.
- H2 테스트만으로 MySQL lock 동작을 검증할 수 없다.

## 다음 조사 우선순위

1. 결제/취소 idempotency와 unique constraint
2. 주문 row 기준 비관적 락
3. Toss 설정 외부화
4. JPA schema 정합성
5. Toss adapter MockWebServer 테스트
