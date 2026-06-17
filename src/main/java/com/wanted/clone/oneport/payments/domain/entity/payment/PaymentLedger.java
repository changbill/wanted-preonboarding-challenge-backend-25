package com.wanted.clone.oneport.payments.domain.entity.payment;

import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PaymentLedger {
    private int id;
    private String siteCode;
    private PgCorp pgCorpName;
    private String transactionId;
    private PaymentMethod method;
    private PaymentStatus paymentStatus;
    private int totalAmount;
    private int balanceAmount;
    private int canceledAmount;
    private int payOutAmount;

    protected PaymentLedger() {
    }

    public boolean isCancellableAmountGreaterThan(int cancellationAmount) {
        return balanceAmount >= cancellationAmount;
    }

    public void verifyCancellableAmount(int cancellationAmount) {
        if (!isCancellableAmountGreaterThan(cancellationAmount)) {
            throw PaymentRuleViolationException.insufficientCancellableAmount(cancellationAmount, balanceAmount);
        }
    }
}
