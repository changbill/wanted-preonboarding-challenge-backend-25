# phase-04 verification

## 검증 결과

- `.\gradlew.bat compileTestJava`: PASS
- `.\gradlew.bat test`: PASS
- gradlew test: PASS

## 추가 확인

- `Order`, `OrderItem`, `PurchaseOrderId`, `PaymentLedger`, `CardPayment`, `TransactionType` 대상 JPA 의존 검색 결과 없음
- mapper 왕복 테스트로 주문/주문상품, 결제 원장, 카드 결제 필드 보존 확인
