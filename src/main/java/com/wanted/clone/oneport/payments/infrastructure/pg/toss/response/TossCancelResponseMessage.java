package com.wanted.clone.oneport.payments.infrastructure.pg.toss.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wanted.clone.oneport.payments.application.result.PaymentCancelResult;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.payment.Cancel;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.payment.TossCommonResponseMessage;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/*
* {
  "mId": "tosspayments",
  "lastTransactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
  "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1",
  "orderId": "MC4wODU4ODQwMzg4NDk0",
  "orderName": "토스 티셔츠 외 2건",
  "taxExemptionAmount": 0,
  "status": "CANCELED",
  "requestedAt": "2024-02-13T12:17:57+09:00",
  "approvedAt": "2024-02-13T12:18:14+09:00",
  "useEscrow": false,
  "cultureExpense": false,
  "card": {
    "issuerCode": "71",
    "acquirerCode": "71",
    "number": "12345678****000*",
    "installmentPlanMonths": 0,
    "isInterestFree": false,
    "interestPayer": null,
    "approveNo": "00000000",
    "useCardPoint": false,
    "cardType": "신용",
    "ownerType": "개인",
    "acquireStatus": "READY",
    "amount": 1000
  },
  "virtualAccount": null,
  "transfer": null,
  "mobilePhone": null,
  "giftCertificate": null,
  "cashReceipt": null,
  "cashReceipts": null,
  "discount": null,
  "cancels": [
    {
      "transactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
      "cancelReason": "테스트 결제 취소",
      "taxExemptionAmount": 0,
      "canceledAt": "2024-02-13T12:20:23+09:00",
      "easyPayDiscountAmount": 0,
      "receiptKey": null,
      "cancelAmount": 1000,
      "taxFreeAmount": 0,
      "refundableAmount": 0,
      "cancelStatus": "DONE",
      "cancelRequestId": null
    }
  ],
  "secret": null,
  "type": "NORMAL",
  "easyPay": {
    "provider": "토스페이",
    "amount": 0,
    "discountAmount": 0
  },
  "easyPayAmount": 0,
  "easyPayDiscountAmount": 0,
  "country": "KR",
  "failure": null,
  "isPartialCancelable": true,
  "receipt": {
    "url": "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tviva20240213121757MvuS8&ref=PX"
  },
  "checkout": {
    "url": "https://api.tosspayments.com/v1/payments/5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1/checkout"
  },
  "currency": "KRW",
  "totalAmount": 1000,
  "balanceAmount": 0,
  "suppliedAmount": 0,
  "vat": 0,
  "taxFreeAmount": 0,
  "method": "카드",
  "version": "2022-11-16"
}
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossCancelResponseMessage extends TossCommonResponseMessage {
    private List<Cancel> cancels;

    private int calculateCanceledTotalAmount(){
        return cancels.stream().map(Cancel::getCancelAmount)
            .reduce(0, Integer::sum);
    }

    public PaymentCancelResult toCommonMessage() {
        int canceledTotalAmount = calculateCanceledTotalAmount();

        return PaymentCancelResult.builder()
            .transactionId(super.getPaymentKey())
            .method(PaymentMethod.fromMethodName(super.getMethod()))
            .status(PaymentStatus.valueOf(super.getStatus()))
            .totalAmount(super.getTotalAmount())
            .balanceAmount(super.getBalanceAmount())
            .canceledAmount(canceledTotalAmount)
            .build();
    }
}
