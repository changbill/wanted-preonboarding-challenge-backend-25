package com.wanted.clone.oneport.payments.application.result;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PaymentCancelResult {
    private String transactionId;
    private PaymentMethod method;
    private PaymentStatus status;
    private int totalAmount;
    private int balanceAmount;
    private int canceledAmount;

    public PaymentLedger toEntity() {
        return PaymentLedger.builder()
            .transactionId(this.transactionId)
            .method(this.method)
            .paymentStatus(this.status)
            .totalAmount(this.totalAmount)
            .balanceAmount(this.balanceAmount)
            .canceledAmount(this.canceledAmount)
            .build();
    }
}
