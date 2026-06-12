# PaymentController API 흐름과 헥사고날 아키텍처 설명

## 1. 먼저 큰 그림부터 보기

이 프로젝트의 결제 기능은 Spring MVC 컨트롤러에서 시작한다.

사용자가 브라우저나 Toss 결제 위젯을 통해 `/payment/...` API를 호출하면, 요청은 다음 순서로 서비스 내부로 들어간다.

```text
브라우저 / Toss redirect
  -> PaymentController
  -> 입력 포트 interface
  -> 애플리케이션 서비스
  -> 출력 포트 interface
  -> Toss API adapter 또는 DB adapter
  -> 도메인 엔티티 상태 변경
  -> DB 저장
  -> Thymeleaf 화면 반환
```

헥사고날 아키텍처 관점으로 보면 다음과 같이 볼 수 있다.

```text
외부 세계
  브라우저, Toss, MySQL

어댑터
  PaymentController
  TossPayment
  OrderRepositoryImpl
  PaymentTransactionLedgerRepository

포트
  PgWidgetUseCase
  PaymentFullfillUseCase
  PaymentAPIs
  OrderRepository
  PaymentLedgerRepository

애플리케이션 내부
  PgWidgetService
  PaymentService
  Order
  PaymentLedger
```

중요한 생각은 하나다.

애플리케이션의 핵심 로직은 HTTP, Toss, MySQL 같은 외부 기술을 직접 몰라도 되어야 한다. 외부 기술은 포트라는 인터페이스 뒤에 숨기고, 실제 구현은 어댑터가 담당한다.

---

## 2. PaymentController의 역할

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/presentation/web/PaymentController.java
```

`PaymentController`는 결제 관련 HTTP 요청을 받는 입력 어댑터다.

```java
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
public class PaymentController {
    private final PgWidgetUseCase pgWidgetUseCase;
    private final PaymentFullfillUseCase paymentFullfillUseCase;
}
```

여기서 중요한 점은 컨트롤러가 구체 클래스인 `PgWidgetService`, `PaymentService`를 직접 필드로 들고 있지 않다는 점이다.

대신 다음 인터페이스에 의존한다.

```java
private final PgWidgetUseCase pgWidgetUseCase;
private final PaymentFullfillUseCase paymentFullfillUseCase;
```

이 인터페이스들이 입력 포트다.

입력 포트는 외부에서 애플리케이션 내부로 들어올 때 사용하는 문이다. 컨트롤러는 "결제 화면을 보여줘", "결제 승인을 처리해줘"라고 포트에 요청하고, 실제 처리는 애플리케이션 서비스가 한다.

---

## 3. `/payment/checkout` 흐름

### 3-1. 사용자가 결제 화면을 요청한다

API:

```http
GET /payment/checkout
```

예시 URL:

```text
http://localhost:8080/payment/checkout?orderId=20241112115994&ordererName=유진호&ordererPhoneNumber=01012341234&userId=jinho123&amount=13400&productName=속이편한우유외1&pgCorpName=toss
```

컨트롤러 메서드:

```java
@GetMapping("checkout")
public String paymentCheckout(
        @RequestParam(value = "orderId") String orderId,
        @RequestParam(value = "ordererName") String ordererName,
        @RequestParam(value = "ordererPhoneNumber") String ordererPhoneNumber,
        @RequestParam(value = "userId") String userId,
        @RequestParam(value = "amount") String amount,
        @RequestParam(value = "productName") String productName,
        @RequestParam(value = "pgCorpName") String pgCorpName,
        Model model
) throws Exception {
    model.addAttribute("orderId", orderId);
    model.addAttribute("ordererName", ordererName);
    model.addAttribute("ordererPhoneNumber", ordererPhoneNumber);
    model.addAttribute("userId", userId);
    model.addAttribute("amount", amount);
    model.addAttribute("productName", productName);
    return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), "checkout");
}
```

이 메서드는 두 가지 일을 한다.

첫째, 화면에서 사용할 값을 `Model`에 넣는다.

```java
model.addAttribute("orderId", orderId);
model.addAttribute("amount", amount);
model.addAttribute("productName", productName);
```

둘째, 어떤 PG사의 checkout 화면을 보여줄지 입력 포트에 물어본다.

```java
return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), "checkout");
```

여기서 `pgCorpName`이 `toss`라면, 결과적으로 `toss/checkout` 템플릿이 반환된다.

---

### 3-2. 입력 포트: PgWidgetUseCase

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/presentation/port/in/PgWidgetUseCase.java
```

```java
public interface PgWidgetUseCase {
    String renderPgUi(PaymentRequest paymentRequest, String pageName) throws Exception;
}
```

이 인터페이스는 컨트롤러가 애플리케이션에 요청할 수 있는 기능을 정의한다.

현재 위치는 `presentation.port.in`이지만, 헥사고날 아키텍처 기준으로는 `application.port.in`에 있는 편이 더 자연스럽다. 입력 포트는 presentation의 소유라기보다 application이 외부에 제공하는 사용 사례이기 때문이다.

---

### 3-3. 애플리케이션 서비스: PgWidgetService

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/application/service/PgWidgetService.java
```

```java
@Service
@RequiredArgsConstructor
public class PgWidgetService implements PgWidgetUseCase {
    private final Set<PgWidget> pgWidgets;
    private final Map<String, PgWidget> pgWidgetSelector = new HashMap<>();
}
```

`PgWidgetService`는 `PgWidgetUseCase`를 구현한다. 즉 컨트롤러가 `PgWidgetUseCase`를 호출하면 실제로는 이 서비스가 실행된다.

Spring은 생성자 주입으로 `PgWidget` 구현체들을 모두 모아서 `Set<PgWidget>`에 넣어준다.

현재 구현체는 Toss용 위젯이다.

```text
TossWidget implements PgWidget
```

서비스가 시작될 때 `@PostConstruct`가 실행된다.

```java
@PostConstruct
public void init() {
    pgWidgets.forEach(pgWidget -> {
        String originalPgName = pgWidget.getClass().getSimpleName().split("Widget")[0].toLowerCase();
        pgWidgetSelector.put(originalPgName, pgWidget);
    });
}
```

`TossWidget`이라는 클래스명이 있으면 다음처럼 map에 저장된다.

```text
key: "toss"
value: TossWidget
```

그 다음 `renderPgUi()`가 호출된다.

```java
@Override
public String renderPgUi(PaymentRequest paymentRequest, String pageType) throws Exception {
    String pgCorpName = Optional.ofNullable(paymentRequest.getPgCorpName())
            .orElseThrow(() -> new IllegalArgumentException("PG Corp Name cannot be null"))
            .toLowerCase();

    PgWidget pgWidget = pgWidgetSelector.get(pgCorpName);
    switch (pageType) {
        case "checkout":
            return pgWidget.checkout();
        case "success":
            return pgWidget.success();
        case "fail":
            return pgWidget.fail();
        default:
            throw new IllegalArgumentException("Invalid pageType name: " + pageType);
    }
}
```

`pgCorpName = toss`, `pageType = checkout`이면 다음 코드가 실행된다.

```java
return pgWidget.checkout();
```

---

### 3-4. 출력 포트: PgWidget

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/application/port/out/pg/PgWidget.java
```

```java
public interface PgWidget {
    String checkout();

    String success();

    String fail();
}
```

`PgWidget`은 출력 포트다.

애플리케이션 서비스 입장에서는 Toss인지, NaverPay인지, KakaoPay인지 직접 알 필요가 없다. 그냥 `PgWidget`에게 "checkout 화면 경로를 줘"라고 요청한다.

---

### 3-5. 출력 어댑터: TossWidget

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/infrastructure/pg/toss/TossWidget.java
```

```java
@Component
public class TossWidget implements PgWidget {
    @Override
    public String checkout() {
        return "toss/checkout";
    }

    @Override
    public String success() {
        return "toss/success";
    }

    @Override
    public String fail() {
        return "toss/fail";
    }
}
```

이 클래스는 Toss 전용 어댑터다. `checkout()`이 `"toss/checkout"`을 반환하면 Spring MVC는 다음 Thymeleaf 템플릿을 찾는다.

```text
src/main/resources/templates/toss/checkout.html
```

최종적으로 사용자는 Toss 결제 위젯이 포함된 checkout 화면을 보게 된다.

---

## 4. `/payment/success` 흐름

### 4-1. Toss 결제 위젯이 성공 redirect를 보낸다

사용자가 Toss 결제창에서 결제를 마치면 Toss는 성공 URL로 redirect한다.

API:

```http
GET /payment/success
```

요청 파라미터:

```text
paymentType
orderId
paymentKey
amount
pgCorpName
```

컨트롤러 메서드:

```java
@GetMapping("success")
public String paymentFullfill(
        @RequestParam(value = "paymentType") String paymentType,
        @RequestParam(value = "orderId") String orderId,
        @RequestParam(value = "paymentKey") String paymentKey,
        @RequestParam(value = "amount") String amount,
        @RequestParam(value = "pgCorpName") String pgCorpName
) throws Exception {

    String result = paymentFullfillUseCase.paymentApproved(ReqPaymentApprove.builder()
        .orderId(orderId)
        .paymentKey(paymentKey)
        .selectedPgCorp(PgCorp.valueOf(pgCorpName.toUpperCase()))
        .totalAmount(Integer.parseInt(amount))
        .build());

    return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), result);
}
```

이 메서드는 checkout보다 더 중요하다.

여기서 실제 결제 승인 요청이 일어난다.

Toss 결제창에서 사용자가 결제를 완료했다고 해서 우리 서버의 결제 처리가 끝난 것은 아니다. 우리 서버가 Toss API에 다시 `paymentKey`, `orderId`, `amount`를 보내서 "이 결제를 승인해도 되는지" 확인해야 한다.

그 일을 하는 코드가 이 부분이다.

```java
String result = paymentFullfillUseCase.paymentApproved(...);
```

---

### 4-2. 컨트롤러가 ReqPaymentApprove를 만든다

```java
ReqPaymentApprove.builder()
    .orderId(orderId)
    .paymentKey(paymentKey)
    .selectedPgCorp(PgCorp.valueOf(pgCorpName.toUpperCase()))
    .totalAmount(Integer.parseInt(amount))
    .build()
```

이 객체에는 결제 승인에 필요한 값이 들어간다.

```text
orderId: 우리 서비스의 주문 ID
paymentKey: Toss가 발급한 결제 키
selectedPgCorp: toss
totalAmount: 승인할 금액
```

초보자 관점에서 보면 이 객체는 "결제 승인 요청서"라고 생각하면 된다.

---

### 4-3. 입력 포트: PaymentFullfillUseCase

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/presentation/port/in/PaymentFullfillUseCase.java
```

```java
public interface PaymentFullfillUseCase {
    String paymentApproved(ReqPaymentApprove requestMessage) throws IOException;
}
```

컨트롤러는 `PaymentService`를 직접 호출하지 않고, 이 인터페이스를 통해 호출한다.

헥사고날 아키텍처에서 입력 포트는 "외부가 애플리케이션에게 요청할 수 있는 기능 목록"이다.

여기서는 다음 기능을 제공한다.

```text
결제 승인 처리하기
```

---

### 4-4. 애플리케이션 서비스: PaymentService

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/application/service/PaymentService.java
```

`PaymentService`는 `PaymentFullfillUseCase`를 구현한다.

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements PaymentFullfillUseCase {
    private final Set<PaymentAPIs> paymentAPIsSet;
    private final Set<TransactionTypeRepository> transactionTypeRepositorySet;
    private final OrderRepository orderRepository;
    private final PaymentLedgerRepository paymentLedgerRepository;
}
```

여기서 의존하는 것들을 보면 중요한 구조가 보인다.

```text
PaymentAPIs
OrderRepository
PaymentLedgerRepository
```

이들은 모두 구체 구현체가 아니라 포트다.

즉 `PaymentService`는 다음 사실을 몰라도 된다.

```text
Toss API를 Retrofit으로 호출하는지
주문을 MySQL에서 조회하는지
결제 원장을 JPA로 저장하는지
```

서비스는 포트만 보고 일한다.

이것이 헥사고날 아키텍처의 핵심에 가깝다.

---

### 4-5. PaymentService 초기화: PG adapter를 map에 등록

`PaymentService`에는 `@PostConstruct` 초기화 코드가 있다.

```java
@PostConstruct
public void init() {
    for (PaymentAPIs paymentAPI : paymentAPIsSet) {
        String pgCorpName = paymentAPI.getClass().getSimpleName().split("Payment")[0].toLowerCase();
        pgAPIs.put(pgCorpName, paymentAPI);
    }

    for (TransactionTypeRepository transactionTypeRepository : transactionTypeRepositorySet) {
        String paymentMethodType = transactionTypeRepository.getClass().getSimpleName().split("TransactionTypeRepository")[0].toLowerCase();
        transactionTypeRepositories.put(paymentMethodType, transactionTypeRepository);
    }
}
```

현재 `PaymentAPIs` 구현체는 `TossPayment`다.

따라서 map에는 다음 값이 들어간다.

```text
key: "toss"
value: TossPayment
```

나중에 `KakaoPayment implements PaymentAPIs`가 추가되면 이론적으로는 다음처럼 추가될 수 있다.

```text
key: "kakao"
value: KakaoPayment
```

다만 현재 방식은 클래스명을 잘라서 key를 만들기 때문에 안전한 방식은 아니다. 더 좋은 방식은 `PaymentAPIs`에 `provider()` 같은 메서드를 두는 것이다.

예:

```java
public interface PaymentAPIs {
    PgProvider provider();
}
```

---

### 4-6. 결제 승인 메서드 실행

핵심 메서드:

```java
@Transactional
@Override
public String paymentApproved(ReqPaymentApprove requestMessage) throws IOException {
    String orderId = requestMessage.getOrderId();
    verifyOrderIsCompleted(orderId);

    paymentAPIs = selectPgAPI(requestMessage.getSelectedPgCorp());
    PaymentApproveResponse response = paymentAPIs.requestPaymentApprove(requestMessage);

    if (paymentAPIs.isPaymentApproved(response.getStatus().name())) {
        Order completedOrder = orderRepository.findById(orderId);
        completedOrder.orderPaymentFullFill(response.getTransactionId());
        paymentLedgerRepository.save(response.toEntity(requestMessage.getSelectedPgCorp()));

        return "success";
    }

    return "fail";
}
```

순서대로 보면 다음과 같다.

```text
1. 요청에서 orderId를 꺼낸다.
2. 주문이 결제 가능한 상태인지 확인한다.
3. pgCorp 값으로 PG adapter를 선택한다.
4. 선택된 PG adapter로 Toss 승인 API를 호출한다.
5. 승인 결과가 성공인지 확인한다.
6. 주문 상태를 결제 완료로 바꾼다.
7. 결제 원장을 저장한다.
8. success 또는 fail 문자열을 반환한다.
```

이 메서드에 붙은 `@Transactional`은 중요하다.

```java
@Transactional
```

이 뜻은 DB 작업을 하나의 transaction으로 묶겠다는 뜻이다.

여기서는 주문 상태 변경과 결제 원장 저장이 같은 transaction 안에서 처리된다.

---

### 4-7. 주문 상태 확인

```java
private void verifyOrderIsCompleted(String orderId) throws IllegalArgumentException {
    OrderStatus status = orderRepository.findById(orderId).getStatus();
    if (!status.equals(OrderStatus.ORDER_COMPLETED))
        throw new IllegalArgumentException("Order is not completed || Order is already paymented");
}
```

주문을 조회해서 상태가 `ORDER_COMPLETED`인지 확인한다.

현재 코드에서 `ORDER_COMPLETED`는 "주문은 생성되었고, 아직 결제 완료 전" 상태로 사용된다.

상태가 다르면 예외를 던진다.

```text
이미 결제된 주문
취소된 주문
존재하지 않는 주문
결제할 수 없는 상태의 주문
```

이런 경우를 막기 위한 단계다.

---

### 4-8. 주문 조회 출력 포트: OrderRepository

`PaymentService`는 주문을 직접 JPA로 조회하지 않는다.

```java
private final OrderRepository orderRepository;
```

`OrderRepository`는 애플리케이션 출력 포트다.

```text
애플리케이션 서비스가 DB 쪽으로 나가기 위해 사용하는 문
```

실제 구현체는 MySQL adapter인 `OrderRepositoryImpl`이다.

```java
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order findById(String id) {
        return jpaOrderRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("OrderId not found"));
    }

    @Override
    public Order save(Order newOrder) {
        return jpaOrderRepository.save(newOrder);
    }
}
```

`PaymentService` 입장에서는 `OrderRepository`만 보인다. 하지만 실제 런타임에서는 Spring이 `OrderRepositoryImpl`을 주입한다.

이 구조 덕분에 나중에 DB가 MySQL에서 다른 저장소로 바뀌어도 서비스 코드는 덜 흔들린다.

---

### 4-9. PG adapter 선택

```java
public PaymentAPIs selectPgAPI(PgCorp pgCorp) {
    return switch (pgCorp.name().toLowerCase()) {
        case "toss" -> pgAPIs.get("toss");
        default -> throw new IllegalArgumentException("Invalid pgCorp name: " + pgCorp.name());
    };
}
```

`PgCorp`가 `TOSS`면 `TossPayment`가 선택된다.

흐름은 다음과 같다.

```text
pgCorpName=toss
  -> PgCorp.TOSS
  -> selectPgAPI(TOSS)
  -> pgAPIs.get("toss")
  -> TossPayment
```

즉 `PaymentService`는 Toss 결제 승인 API를 직접 호출하지 않고, `PaymentAPIs`라는 포트를 통해 호출한다.

---

### 4-10. 출력 포트: PaymentAPIs

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/application/port/out/pg/PaymentAPIs.java
```

```java
public interface PaymentAPIs {
    PaymentApproveResponse requestPaymentApprove(ReqPaymentApprove requestMessage) throws IOException;

    boolean isPaymentApproved(String status);

    TossCancelResponseMessage requestPaymentCancel(String txId, ReqCancelOrder requestMessage) throws IOException;

    List<TossSettlementsResponseMessage> requestPaymentSettlement() throws IOException;
}
```

이 인터페이스는 애플리케이션이 PG사 API를 호출하기 위해 사용하는 출력 포트다.

출력 포트는 내부 서비스가 바깥으로 나갈 때 사용하는 문이다.

이상적인 방향은 다음과 같다.

```text
PaymentService
  -> PaymentAPIs interface
      -> TossPayment
      -> KakaoPayment
      -> NaverPayment
```

하지만 현재 코드에는 개선할 점이 있다.

`PaymentAPIs`가 `ReqPaymentApprove`, `ReqCancelOrder`, `TossCancelResponseMessage`, `TossSettlementsResponseMessage`를 직접 알고 있다.

즉 포트가 웹 DTO와 Toss 전용 DTO에 오염되어 있다.

헥사고날 아키텍처를 더 잘 지키려면 다음처럼 바꾸는 것이 좋다.

```text
ReqPaymentApprove
  -> ApprovePaymentCommand

TossApproveResponseMessage
  -> PaymentApprovalResult

TossCancelResponseMessage
  -> PaymentCancelResult
```

즉 애플리케이션 포트는 Toss나 웹을 모르는 타입을 사용해야 한다.

---

### 4-11. Toss 출력 어댑터: TossPayment

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/infrastructure/pg/toss/TossPayment.java
```

```java
@Component
@RequiredArgsConstructor
public class TossPayment implements PaymentAPIs {
    private final TossPaymentAPIs tossClient;
}
```

`TossPayment`는 `PaymentAPIs`를 구현한다.

즉 애플리케이션 서비스가 `PaymentAPIs`를 호출하면 실제로는 `TossPayment`가 실행된다.

결제 승인 부분:

```java
@Override
public PaymentApproveResponse requestPaymentApprove(ReqPaymentApprove requestMessage) throws IOException {
    TossApproveMessage message = TossApproveMessage.from(requestMessage);
    Response<TossApproveResponseMessage> response = tossClient.paymentFullfill(message).execute();
    if (response.isSuccessful())
        return Objects.requireNonNull(response.body())
                .toCommonMessage();

    throw new IOException(response.message());
}
```

여기서 하는 일은 세 단계다.

```text
1. 우리 서버의 승인 요청 DTO를 Toss 요청 DTO로 변환한다.
2. Retrofit client로 Toss 승인 API를 호출한다.
3. Toss 응답 DTO를 공통 승인 응답으로 변환한다.
```

코드로 보면 다음과 같다.

```java
TossApproveMessage message = TossApproveMessage.from(requestMessage);
```

`ReqPaymentApprove`를 Toss가 원하는 요청 형식인 `TossApproveMessage`로 바꾼다.

```java
Response<TossApproveResponseMessage> response =
    tossClient.paymentFullfill(message).execute();
```

실제 Toss API를 호출한다.

```java
return Objects.requireNonNull(response.body()).toCommonMessage();
```

Toss 응답을 `PaymentApproveResponse`로 바꿔서 애플리케이션 서비스에 돌려준다.

---

### 4-12. Retrofit client: TossPaymentAPIs

파일:

```text
src/main/java/com/wanted/clone/oneport/payments/infrastructure/pg/toss/TossPaymentAPIs.java
```

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

이 인터페이스는 Retrofit이 사용하는 HTTP client 정의다.

`@POST("payments/confirm")`는 실제 Toss 결제 승인 API 경로다.

즉 이 프로젝트에서 실제 외부 HTTP 요청은 이 지점에서 만들어진다.

흐름을 다시 쓰면 다음과 같다.

```text
PaymentController
  -> PaymentFullfillUseCase
  -> PaymentService
  -> PaymentAPIs
  -> TossPayment
  -> TossPaymentAPIs
  -> Toss 서버
```

---

### 4-13. 승인 성공 후 주문 상태 변경

Toss 승인 응답이 성공이면 `PaymentService`는 주문 상태를 바꾼다.

```java
if (paymentAPIs.isPaymentApproved(response.getStatus().name())) {
    Order completedOrder = orderRepository.findById(orderId);
    completedOrder.orderPaymentFullFill(response.getTransactionId());
    paymentLedgerRepository.save(response.toEntity(requestMessage.getSelectedPgCorp()));

    return "success";
}
```

먼저 주문을 다시 조회한다.

```java
Order completedOrder = orderRepository.findById(orderId);
```

그 다음 주문에 결제 완료 처리를 한다.

```java
completedOrder.orderPaymentFullFill(response.getTransactionId());
```

`Order` 엔티티의 메서드는 다음과 같다.

```java
public void orderPaymentFullFill(String paymentKey) {
    update(OrderStatus.PAYMENT_FULLFILL);
    this.paymentId = paymentKey;
}
```

이 메서드는 주문 상태를 `PAYMENT_FULLFILL`로 변경하고, 결제 ID를 저장한다.

여기서 중요한 점은 상태 변경 로직이 서비스에만 있지 않고 `Order` 도메인 객체 안에 있다는 점이다.

헥사고날 아키텍처에서 도메인은 애플리케이션의 핵심 규칙을 담는 곳이다. 따라서 "주문이 결제 완료되면 어떤 상태가 되어야 하는가" 같은 규칙은 도메인 객체 안에 있는 것이 자연스럽다.

---

### 4-14. 결제 원장 저장

주문 상태를 바꾼 뒤 결제 원장을 저장한다.

```java
paymentLedgerRepository.save(response.toEntity(requestMessage.getSelectedPgCorp()));
```

`PaymentApproveResponse`를 `PaymentLedger` 엔티티로 바꾼다.

```java
public PaymentLedger toEntity(PgCorp corp) {
    return PaymentLedger.builder()
        .transactionId(this.transactionId)
        .method(this.getMethod())
        .paymentStatus(this.getStatus())
        .totalAmount(this.getTotalAmount())
        .balanceAmount(this.getBalanceAmount())
        .canceledAmount(0)
        .pgCorpName(corp)
        .build();
}
```

그 다음 `PaymentLedgerRepository` 출력 포트를 통해 저장한다.

실제 구현체는 다음 클래스다.

```text
PaymentTransactionLedgerRepository
```

```java
@Repository
@RequiredArgsConstructor
public class PaymentTransactionLedgerRepository implements PaymentLedgerRepository {
    private final JpaPaymentLedgerRepository jpaPaymentLedgerRepository;

    @Override
    public void save(PaymentLedger paymentLedgerInfo) {
        jpaPaymentLedgerRepository.save(paymentLedgerInfo);
    }
}
```

다시 말해 `PaymentService`는 JPA repository를 직접 모르고, `PaymentLedgerRepository`라는 포트를 통해 저장한다.

---

### 4-15. 성공/실패 화면 반환

`paymentApproved()`가 `"success"`를 반환하면 컨트롤러는 다시 PG 위젯 서비스에 화면을 요청한다.

```java
return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), result);
```

`result = "success"`이면 다음 흐름이 된다.

```text
pgWidgetUseCase.renderPgUi(toss, success)
  -> PgWidgetService
  -> TossWidget.success()
  -> "toss/success"
```

최종적으로 이 템플릿이 렌더링된다.

```text
src/main/resources/templates/toss/success.html
```

승인 실패면 `"fail"`이 반환되고, 최종 템플릿은 다음이 된다.

```text
src/main/resources/templates/toss/fail.html
```

---

## 5. `/payment/fail` 흐름

API:

```http
GET /payment/fail
```

컨트롤러 메서드:

```java
@GetMapping("fail")
public String paymentFail(
        @RequestParam(value = "message") String message,
        @RequestParam(value = "message") String pgCorpName
) throws Exception {
    return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), "fail");
}
```

실패 화면을 렌더링하는 API다.

다만 현재 코드에는 문제가 있다.

```java
@RequestParam(value = "message") String message,
@RequestParam(value = "message") String pgCorpName
```

두 파라미터가 모두 `"message"`를 보고 있다.

의도는 아마 다음과 같았을 가능성이 높다.

```java
@RequestParam(value = "message") String message,
@RequestParam(value = "pgCorpName") String pgCorpName
```

현재 상태에서는 `pgCorpName`에도 실패 메시지가 들어가고, `PaymentRequest.of(pgCorpName)`에서 PG enum 변환이 실패할 수 있다.

---

## 6. `/payment/confirm` 흐름

API:

```http
POST /payment/confirm
```

컨트롤러 메서드:

```java
@PostMapping("confirm")
public String paymentApprove(@RequestBody ReqPaymentApprove message) {
    log.info("message -> {}", message);
    return "toss/fail";
}
```

현재 이 API는 실제 결제 승인 처리를 하지 않는다.

요청 body를 로그로 찍고 항상 `"toss/fail"`을 반환한다.

즉 실제 승인 흐름은 현재 `/payment/success`에서 처리된다.

---

## 7. 헥사고날 아키텍처를 초보자 관점에서 이해하기

### 7-1. 왜 헥사고날 아키텍처가 필요한가

초보자는 보통 다음처럼 코드를 작성하기 쉽다.

```text
Controller
  -> Service
  -> Toss API 직접 호출
  -> JpaRepository 직접 호출
```

처음에는 단순해서 좋아 보인다.

하지만 시간이 지나면 문제가 생긴다.

예를 들어 Toss 말고 다른 PG사를 추가해야 한다면 어떻게 될까?

```text
if pg == toss:
    Toss API 호출
else if pg == kakao:
    Kakao API 호출
else if pg == naver:
    Naver API 호출
```

이런 코드가 서비스 안에 계속 늘어난다.

DB가 바뀌거나, 테스트에서 가짜 PG를 쓰고 싶거나, 결제 승인 방식을 바꾸고 싶을 때도 서비스 코드가 계속 흔들린다.

헥사고날 아키텍처는 이런 문제를 줄이기 위해 내부와 외부를 나눈다.

---

### 7-2. 내부와 외부

헥사고날 아키텍처에서 가장 중요한 구분은 이것이다.

```text
내부: 비즈니스 규칙
외부: 기술 세부사항
```

이 프로젝트에서는 내부가 다음에 가깝다.

```text
PaymentService
Order
PaymentLedger
결제 승인 규칙
주문 상태 변경 규칙
취소 가능 금액 규칙
```

외부는 다음이다.

```text
HTTP 요청
Toss API
MySQL
Thymeleaf
Retrofit
JPA
```

핵심은 내부 코드가 외부 기술에 덜 의존하도록 만드는 것이다.

---

### 7-3. 포트와 어댑터

헥사고날 아키텍처는 "Ports and Adapters"라고도 부른다.

한국어로 풀면 "문과 변환기" 정도로 이해할 수 있다.

포트는 인터페이스다.

```java
public interface PaymentFullfillUseCase {
    String paymentApproved(ReqPaymentApprove requestMessage) throws IOException;
}
```

어댑터는 그 포트를 사용하는 외부 코드 또는 구현하는 외부 코드다.

입력 어댑터:

```text
PaymentController
```

입력 포트:

```text
PaymentFullfillUseCase
```

애플리케이션 서비스:

```text
PaymentService
```

출력 포트:

```text
PaymentAPIs
OrderRepository
PaymentLedgerRepository
```

출력 어댑터:

```text
TossPayment
OrderRepositoryImpl
PaymentTransactionLedgerRepository
```

---

### 7-4. 입력 포트와 출력 포트 차이

입력 포트는 외부가 내부를 호출하기 위한 문이다.

예:

```text
결제 승인하기
결제 화면 보여주기
주문 생성하기
```

현재 코드:

```text
PgWidgetUseCase
PaymentFullfillUseCase
CreateNewOrderUseCase
```

출력 포트는 내부가 외부 기능을 사용하기 위한 문이다.

예:

```text
PG사 결제 승인 요청하기
주문 DB 조회하기
결제 원장 저장하기
```

현재 코드:

```text
PaymentAPIs
OrderRepository
PaymentLedgerRepository
PgWidget
```

---

### 7-5. 이 프로젝트의 결제 승인 흐름을 헥사고날로 다시 보기

```text
[입력 어댑터]
PaymentController

  사용자의 HTTP 요청을 받는다.
  HTTP query parameter를 Java 객체로 바꾼다.

        |
        v

[입력 포트]
PaymentFullfillUseCase

  외부에서 애플리케이션으로 들어올 수 있는 기능을 정의한다.

        |
        v

[애플리케이션 서비스]
PaymentService

  주문 상태를 확인한다.
  PG adapter를 선택한다.
  결제 승인을 요청한다.
  주문 상태를 결제 완료로 바꾼다.
  결제 원장을 저장한다.

        |
        v

[출력 포트]
PaymentAPIs
OrderRepository
PaymentLedgerRepository

  외부 PG, DB로 나가는 인터페이스다.

        |
        v

[출력 어댑터]
TossPayment
OrderRepositoryImpl
PaymentTransactionLedgerRepository

  실제 Toss API, JPA, MySQL을 사용한다.
```

---

## 8. 현재 코드가 헥사고날에 맞는 부분

### 컨트롤러가 서비스 구현체보다 입력 포트를 의존한다

```java
private final PgWidgetUseCase pgWidgetUseCase;
private final PaymentFullfillUseCase paymentFullfillUseCase;
```

이 구조는 좋다.

컨트롤러는 "누가 처리하는지"보다 "무슨 기능을 호출하는지"에 집중한다.

### 애플리케이션 서비스가 출력 포트를 의존한다

```java
private final OrderRepository orderRepository;
private final PaymentLedgerRepository paymentLedgerRepository;
private final Set<PaymentAPIs> paymentAPIsSet;
```

이 구조도 좋다.

서비스가 JPA나 Retrofit을 직접 호출하지 않고 포트를 통해 호출한다.

### Toss 구현이 infrastructure에 있다

```text
payments/infrastructure/pg/toss
```

Toss는 외부 시스템이다. 따라서 infrastructure 아래에 있는 것이 자연스럽다.

### MySQL 구현이 infrastructure에 있다

```text
payments/infrastructure/persistence/mysql
```

MySQL과 JPA도 외부 기술이다. 따라서 infrastructure 아래에 있는 것이 자연스럽다.

---

## 9. 현재 코드에서 헥사고날을 더 잘 지키려면

### 9-1. 입력 포트 위치 변경

현재:

```text
payments/presentation/port/in
```

권장:

```text
payments/application/port/in
```

입력 포트는 presentation이 아니라 application의 기능 목록이기 때문이다.

---

### 9-2. 웹 DTO를 애플리케이션 내부로 넘기지 않기

현재:

```java
String paymentApproved(ReqPaymentApprove requestMessage)
```

`ReqPaymentApprove`는 웹 요청 DTO다.

권장:

```java
String paymentApproved(ApprovePaymentCommand command)
```

컨트롤러에서 웹 DTO를 application command로 변환한 뒤 서비스에 넘기는 편이 좋다.

```text
HTTP request
  -> ReqPaymentApprove
  -> ApprovePaymentCommand
  -> PaymentService
```

이렇게 하면 나중에 gRPC, batch, message queue로 결제 승인을 호출해도 `PaymentService`를 그대로 사용할 수 있다.

---

### 9-3. Toss DTO를 포트 밖으로 밀어내기

현재 `PaymentAPIs`는 Toss 응답 타입을 노출한다.

```java
TossCancelResponseMessage requestPaymentCancel(...)
List<TossSettlementsResponseMessage> requestPaymentSettlement()
```

권장:

```java
PaymentCancelResult requestPaymentCancel(...)
List<PaymentSettlementResult> requestPaymentSettlement(...)
```

Toss 응답을 공통 결과로 바꾸는 책임은 `TossPayment` adapter가 가져야 한다.

---

### 9-4. PG 선택 방식을 클래스명에 의존하지 않기

현재:

```java
paymentAPI.getClass().getSimpleName().split("Payment")[0].toLowerCase()
```

권장:

```java
public interface PaymentAPIs {
    PgProvider provider();
}
```

그리고 구현체는 직접 자신이 어떤 PG인지 말한다.

```java
@Override
public PgProvider provider() {
    return PgProvider.TOSS;
}
```

이렇게 하면 클래스명을 바꿔도 동작이 깨지지 않는다.

---

### 9-5. PaymentService의 mutable field 제거

현재:

```java
public PaymentAPIs paymentAPIs;
```

그리고 요청 처리 중 필드에 값을 넣는다.

```java
paymentAPIs = selectPgAPI(requestMessage.getSelectedPgCorp());
```

이건 위험하다.

`PaymentService`는 Spring singleton bean이다. 여러 요청이 동시에 들어오면 이 필드를 공유한다.

권장:

```java
PaymentAPIs paymentAPIs = selectPgAPI(requestMessage.getSelectedPgCorp());
```

요청마다 지역 변수로 사용해야 한다.

---

## 10. 전체 흐름 요약

### 결제 화면 요청

```text
GET /payment/checkout
  -> PaymentController.paymentCheckout()
  -> PgWidgetUseCase.renderPgUi()
  -> PgWidgetService.renderPgUi()
  -> PgWidget.checkout()
  -> TossWidget.checkout()
  -> "toss/checkout"
  -> templates/toss/checkout.html 렌더링
```

### 결제 승인 요청

```text
GET /payment/success
  -> PaymentController.paymentFullfill()
  -> ReqPaymentApprove 생성
  -> PaymentFullfillUseCase.paymentApproved()
  -> PaymentService.paymentApproved()
  -> OrderRepository.findById()
  -> 주문 상태 확인
  -> PaymentAPIs 선택
  -> TossPayment.requestPaymentApprove()
  -> TossApproveMessage 변환
  -> TossPaymentAPIs.paymentFullfill()
  -> Toss 서버 payments/confirm 호출
  -> TossApproveResponseMessage 수신
  -> PaymentApproveResponse 변환
  -> Order.orderPaymentFullFill()
  -> PaymentLedger 생성
  -> PaymentLedgerRepository.save()
  -> "success"
  -> PgWidgetService.renderPgUi()
  -> "toss/success"
```

### 결제 실패 화면 요청

```text
GET /payment/fail
  -> PaymentController.paymentFail()
  -> PgWidgetUseCase.renderPgUi()
  -> PgWidgetService.renderPgUi()
  -> TossWidget.fail()
  -> "toss/fail"
```

---

## 11. 한 문장으로 정리

`PaymentController`는 HTTP 요청을 받는 입구이고, 실제 결제 업무는 입력 포트를 통해 `PaymentService`로 들어간다. `PaymentService`는 주문 상태 확인, PG 승인 요청, 주문 상태 변경, 결제 원장 저장을 처리하며, Toss API와 DB는 각각 출력 포트 뒤의 어댑터가 담당한다. 이 구조가 헥사고날 아키텍처의 기본 모양이다.
