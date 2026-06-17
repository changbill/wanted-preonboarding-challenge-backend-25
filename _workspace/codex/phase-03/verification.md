# Verification

```text
gradlew.bat test: PASS
```

## 결과

- 전체 테스트 통과
- 결제 승인 idempotency 테스트 통과
- 중복 `paymentKey` 승인 거부 테스트 통과
- 주문 전체/부분 취소 상태 전이 테스트 통과
- 취소 가능 금액 검증 테스트 통과
