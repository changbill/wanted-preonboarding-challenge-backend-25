# HTTP 클라이언트: Retrofit vs Spring WebClient vs RestTemplate

## 1. 요약

서버에서 다른 서버(Toss PG API 등)로 HTTP 요청을 보낼 때 사용할 수 있는 대표적인 도구는 **Retrofit**, **Spring WebClient**, **RestTemplate** 세 가지다. 셋 다 같은 일(HTTP 요청/응답)을 하지만, **설계 철학·의존성·호출 방식**이 다르다.

이 프로젝트(`wanted_challenge_25`)는 Toss PG 연동에 **Retrofit + OkHttp**를 사용한다. 아래에서 세 도구를 비교하고, 이 코드베이스에서 Retrofit을 선택했을 **타당한 이유**를 유추한다.

---

## 2. 세 도구 한눈에 비교

| 항목 | Retrofit | Spring WebClient | RestTemplate |
|------|----------|------------------|--------------|
| 제공 주체 | Square (OkHttp 기반) | Spring Framework | Spring Framework |
| 선언 방식 | 인터페이스 + 어노테이션 (`@POST`, `@GET` 등) | 메서드 체이닝 또는 함수형 빌더 | 메서드 호출 (`getForObject`, `postForEntity` 등) |
| 기본 호출 모델 | 동기(`execute()`) / 비동기(`enqueue()`) | 비동기(리액티브, `Mono`/`Flux`) 중심 | 동기 |
| Spring 의존성 | 없음 (독립 라이브러리) | `spring-webflux` 권장 | `spring-web` (내장) |
| JSON 처리 | Converter 플러그인 (Jackson, Gson 등) | `Codec` (Jackson 기본) | `HttpMessageConverter` |
| HTTP 엔진 | OkHttp (교체 가능) | Reactor Netty, Jetty, Apache 등 | JDK `HttpURLConnection` → Apache HttpClient 등 |
| Spring Boot 상태 | 서드파티 | **권장(현행)** | **유지보수 모드(비권장)** |
| API 스펙 문서화 | 인터페이스 자체가 API 계약서 역할 | URL·바디를 코드에 직접 작성 | URL·바디를 코드에 직접 작성 |
| 인터셉터/필터 | OkHttp `Interceptor` | `ExchangeFilterFunction` | `ClientHttpRequestInterceptor` |

---

## 3. 각 도구 상세

### 3-1. Retrofit

**핵심 아이디어:** HTTP API를 **Java 인터페이스**로 선언하면, Retrofit이 구현체를 런타임에 생성한다.

```java
public interface TossPaymentAPIs {
    @POST("payments/confirm")
    Call<TossApproveResponseMessage> paymentFullfill(@Body TossApproveMessage requestMessage);

    @POST("payments/{paymentKey}/cancel")
    Call<TossCancelResponseMessage> paymentCancel(
        @Path("paymentKey") String paymentKey,
        @Body TossCancelMessage requestMessage
    );
}
```

**장점**
- 엔드포인트·HTTP 메서드·경로 변수·요청 바디가 **한 인터페이스에 모여** 읽기 쉽다.
- 외부 API 스펙(토스 API 문서)과 **1:1로 대응**하기 좋다.
- OkHttp `Interceptor`로 인증 헤더, 로깅, 타임아웃을 **클라이언트 단위로** 묶을 수 있다.
- Spring과 **느슨하게 결합**된다. 인프라 계층 어댑터에만 두기 적합하다.

**단점**
- Spring Boot에 내장되지 않아 **별도 의존성·Bean 설정**이 필요하다.
- 동기 `execute()`는 호출 스레드를 블로킹한다.
- Spring 생태계(Actuator 메트릭, `@LoadBalanced` 등)와의 통합은 WebClient/Feign보다 수동 설정이 많다.

**이 프로젝트에서의 사용 예**

```text
TossApiClientConfig
  -> OkHttpClient (Basic Auth 인터셉터, 타임아웃)
  -> Retrofit (baseUrl, JacksonConverterFactory)
  -> TossPaymentAPIs (retrofit.create)

TossPayment (PaymentAPIs 어댑터)
  -> tossClient.paymentFullfill(message).execute()
```

---

### 3-2. Spring WebClient

**핵심 아이디어:** Spring 5부터 도입된 **비동기·논블로킹** HTTP 클라이언트. `RestTemplate`의 후속으로 권장된다.

```java
// 동기(block) 호출도 가능하지만, 설계 의도는 리액티브
TossApproveResponseMessage response = webClient.post()
    .uri("/v1/payments/confirm")
    .bodyValue(message)
    .retrieve()
    .bodyToMono(TossApproveResponseMessage.class)
    .block();
```

**장점**
- Spring Boot **공식 권장** HTTP 클라이언트.
- `Mono`/`Flux` 기반으로 **동시 다발 호출·스트리밍**에 유리하다.
- `ExchangeFilterFunction`으로 인증·로깅·재시도를 체이닝할 수 있다.
- 테스트 시 `WebClient` mock/`MockWebServer` 연동이 자연스럽다.

**단점**
- 리액티브를 제대로 쓰려면 **WebFlux 스택**과 사고방식이 필요하다.
- `.block()`으로 동기화하면 리액티브 이점이 줄어든다.
- URL·경로를 **메서드 체이닝에 흩뿌리면** Toss API처럼 엔드포인트가 여러 개일 때 계약이 분산된다.
- 이 프로젝트는 **Spring MVC(서블릿) + 동기 JPA** 중심이라 WebClient의 강점을 크게 쓰기 어렵다.

---

### 3-3. RestTemplate

**핵심 아이디어:** Spring 3 시대부터 쓰인 **동기식** HTTP 클라이언트.

```java
ResponseEntity<TossApproveResponseMessage> response = restTemplate.postForEntity(
    "https://api.tosspayments.com/v1/payments/confirm",
    message,
    TossApproveResponseMessage.class
);
```

**장점**
- `spring-boot-starter-web`만으로 사용 가능해 **설정이 단순**하다.
- 동기 코드·JPA 트랜잭션과 **그대로 맞물린다**.
- 팀 내 Spring 경험이 많으면 진입 장벽이 낮다.

**단점**
- Spring 5.0 이후 **유지보수 모드(maintenance mode)** — 신규 프로젝트에 비권장.
- API 정의가 **문자열 URL + 메서드 호출**에 흩어져 TossPaymentAPIs 같은 **계약 인터페이스**를 만들기 어렵다.
- 연결 풀·타임아웃·인터셉터 설정이 WebClient/OkHttp보다 다루기 불편한 편이다.
- 멀티 PG 확장 시 PG별 URL·헤더·직렬화 규칙이 서비스 코드에 섞이기 쉽다.

---

## 4. 같은 Toss 승인 API를 각각으로 호출한다면

### Retrofit (현재 프로젝트)

```java
Response<TossApproveResponseMessage> response =
    tossClient.paymentFullfill(message).execute();
```

### WebClient (가상)

```java
TossApproveResponseMessage body = webClient.post()
    .uri("https://api.tosspayments.com/v1/payments/confirm")
    .header("Authorization", basicAuth)
    .bodyValue(message)
    .retrieve()
    .bodyToMono(TossApproveResponseMessage.class)
    .block();
```

### RestTemplate (가상)

```java
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", basicAuth);
HttpEntity<TossApproveMessage> entity = new HttpEntity<>(message, headers);

ResponseEntity<TossApproveResponseMessage> response = restTemplate.exchange(
    "https://api.tosspayments.com/v1/payments/confirm",
    HttpMethod.POST,
    entity,
    TossApproveResponseMessage.class
);
```

동작 결과는 같지만, **API 계약이 어디에 모이는지**가 다르다. Retrofit은 `TossPaymentAPIs` 인터페이스 한곳에, 나머지는 호출 코드마다 URL·헤더·바디 조립이 반복된다.

---

## 5. 이 프로젝트에서 Retrofit을 선택했을 타당한 이유 (유추)

코드와 `SPEC.md`를 기준으로 한 **합리적 추론**이다. 당시 의사결정 문서가 없으므로 "확정 사실"이 아니라 "왜 이렇게 짰는지 설명 가능한 이유"로 본다.

### 5-1. 헥사고날 아키텍처와 PG 어댑터 분리

이 프로젝트는 결제 흐름을 **입력 포트 → 애플리케이션 서비스 → 출력 포트 → 어댑터**로 나눈다.

```text
PaymentService
  -> PaymentAPIs (출력 포트)
    -> TossPayment (어댑터)
      -> TossPaymentAPIs (Retrofit 인터페이스)
```

`PaymentService`는 "결제 승인을 요청한다"는 **도메인/애플리케이션 언어**만 알고, HTTP 세부사항은 `infrastructure.pg.toss`에 격리한다. Retrofit 인터페이스는 **외부 시스템(Toss) 전용 계약**으로 두기 좋고, Spring Web 스택과 분리된다.

다수 PG 통합이 목표(`SPEC.md`: "다수 PG사의 결제 서비스를 하나의 API로 통합")이므로, PG마다 `XxxPaymentAPIs` 인터페이스 + `XxxPayment` 어댑터를 추가하는 패턴이 자연스럽다.

### 5-2. Toss Open API 문서와 1:1 매핑

토스 API는 `POST /v1/payments/confirm`, `POST /v1/payments/{paymentKey}/cancel`처럼 **REST 엔드포인트가 명확**하다. Retrofit의 `@POST`, `@Path`, `@Body`는 공식 문서를 **그대로 코드로 옮기기** 쉽다.

`TossPaymentAPIs` 한 파일을 보면 이 PG가 지원하는 HTTP API 전체를 파악할 수 있어, 온보딩·리뷰·스펙 변경 추적에 유리하다.

### 5-3. OkHttp 인터셉터로 PG 인증을 한곳에 모음

`TossApiClientConfig`는 Toss **시크릿 키 Basic Auth**를 OkHttp `Interceptor`로 붙인다.

```java
.addInterceptor(chain -> {
    Request request = chain.request().newBuilder()
        .addHeader("Authorization", authorizations)
        .build();
    return chain.proceed(request);
})
```

PG마다 인증 방식이 다를 수 있다(토스: Basic, 다른 PG: API Key, OAuth 등). **클라이언트 Bean 단위**로 묶기에 Retrofit + OkHttp 조합이 잘 맞는다. `PaymentService`나 컨트롤러에 인증 헤더 로직이 새지 않는다.

### 5-4. 동기식 결제 플로우와 잘 맞음

현재 `TossPayment`는 `call.execute()`로 **동기 호출**한다. 결제 승인은:

1. 주문 검증
2. PG 승인 API 호출
3. 성공 시 주문 상태 변경 + 원장 저장 (`@Transactional`)

의 **순차적 트랜잭션** 흐름이다. WebClient의 논블로킹·리액티브 이점을 쓸 만한 **고동시·다건 병렬 호출** 시나리오가 이 경로에는 없다. 오히려 WebClient를 `.block()`으로 쓰면 설정만 복잡해질 수 있다.

### 5-4. RestTemplate을 피한 이유

- Spring이 **신규 개발에 RestTemplate 비권장**을 명시했다.
- URL 문자열 기반이라 `TossPaymentAPIs` 같은 **선언적 API 경계**를 만들기 어렵다.
- 프로젝트가 Spring Boot 3.2 + Java 21로 **비교적 최신 스택**인 점을 고려하면, 레거시 클라이언트를 새로 도입할 이유가 약하다.

### 5-5. WebClient를 굳이 쓰지 않은 이유

- 런타임은 **Spring MVC + JPA(블로킹)** 중심이다. WebFlux 전환 없이 WebClient만 끼워 넣으면 이득이 제한적이다.
- `spring-boot-starter-webflux` 추가는 **의존성·스레드 모델** 측면에서 불필요한 복잡도를 올릴 수 있다.
- 외부 PG HTTP 호출은 **인프라 관심사**이고, Retrofit은 그 레이어에만 두면 된다. 애플리케이션 전체를 Spring Reactive로 끌고 갈 필요가 없다.

### 5-6. Jackson DTO와의 조합

프로젝트는 이미 Jackson(`@JsonNaming`, `JavaTimeModule` 등)을 광범위하게 쓴다. `JacksonConverterFactory`로 Retrofit 응답을 `TossApproveResponseMessage` 등 **인프라 DTO**에 바로 매핑하고, `toCommonMessage()`로 애플리케이션 DTO로 변환하는 흐름이 정리되어 있다.

---

## 6. 만약 다른 도구를 썼다면?

| 선택 | 이 프로젝트에 미칠 영향 |
|------|-------------------------|
| **WebClient** | 가능하지만 WebFlux 의존·리액티브 학습 비용 대비 이득이 작음. API 계약을 인터페이스로 모으려면 래퍼 클래스를 직접 설계해야 함. |
| **RestTemplate** | 빠르게 붙일 수 있으나 비권장 스택. PG가 늘수록 URL·헤더 중복이 커짐. |
| **OpenFeign** | Spring Cloud 환경에서 Retrofit과 유사한 선언적 스타일. 단일 PG HTTP 어댑터만 필요한 현재 규모에서는 Retrofit이 더 가볍다. |
| **Java HttpClient (JDK 11+)** | 의존성 제로지만 보일러플레이트가 많고, `TossPaymentAPIs` 수준의 선언적 계약이 없음. |

---

## 7. 결론

- **Retrofit, WebClient, RestTemplate 모두** 서버→서버 HTTP 호출에 쓸 수 있다. **필수는 아니다.**
- **RestTemplate**은 레거시·유지보수 모드라 신규 PG 연동에는 맞지 않다.
- **WebClient**는 Spring 공식 권장이지만, **리액티브·고동시** 환경에서 진가를 발휘한다. 이 프로젝트의 **동기 결제 트랜잭션**에는 과한 선택일 수 있다.
- **Retrofit**은 이 코드베이스의 다음 요구와 잘 맞는다.
  1. 헥사고날 아키텍처에서 **PG HTTP 계약을 인프라에 격리**
  2. Toss API 문서와 **선언적으로 1:1 매핑**
  3. OkHttp로 **PG별 인증·타임아웃 설정 캡슐화**
  4. **동기 승인/취소** 플로우에 단순하게 대응
  5. 다수 PG 확장 시 **PG마다 `XxxPaymentAPIs` 인터페이스 추가** 패턴

따라서 "Spring이니까 WebClient여야 한다"거나 "HTTP면 RestTemplate이면 된다"기보다, **외부 PG API를 어댑터 경계에 선언적으로 묶는 것**이 이 프로젝트 구조에서는 Retrofit이 가장 설득력 있는 선택으로 보인다.

---

## 8. 참고: 이 프로젝트 관련 파일

| 파일 | 역할 |
|------|------|
| `TossPaymentAPIs.java` | Retrofit API 인터페이스 |
| `TossApiClientConfig.java` | OkHttp + Retrofit Bean 설정 |
| `TossPayment.java` | `PaymentAPIs` 어댑터, `.execute()` 호출 |
| `PaymentService.java` | 출력 포트를 통한 PG 승인 오케스트레이션 |
| `build.gradle.kts` | `retrofit`, `converter-jackson`, OkHttp 의존성 |

관련 흐름은 [payment-controller-flow.md](./payment-controller-flow.md)를 함께 보면 된다.
