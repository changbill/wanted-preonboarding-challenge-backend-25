package com.wanted.clone.oneport.payments.application.command;

import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApprovePaymentCommand {
    private String siteCode;
    private PgCorp selectedPgCorp;
    private String paymentKey;
    private String orderId;
    private int totalAmount;
}
