package com.wanted.clone.oneport.payments.application.result;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PaymentApprovalResult {
    private String transactionId;
    private PaymentMethod method;
    private PaymentStatus status;
    private int totalAmount;
    private int balanceAmount;

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
}
