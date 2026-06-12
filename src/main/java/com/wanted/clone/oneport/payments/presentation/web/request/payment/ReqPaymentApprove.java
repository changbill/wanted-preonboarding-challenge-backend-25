package com.wanted.clone.oneport.payments.presentation.web.request.payment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReqPaymentApprove {
    private String siteCode;
    private PgCorp selectedPgCorp;
    private String paymentKey; // transaction key tno
    private String orderId;
    private int totalAmount;

    public ApprovePaymentCommand toCommand() {
        return ApprovePaymentCommand.builder()
            .siteCode(siteCode)
            .selectedPgCorp(selectedPgCorp)
            .paymentKey(paymentKey)
            .orderId(orderId)
            .totalAmount(totalAmount)
            .build();
    }

    @Override
    public String toString(){
        return "PaymentApproveMessage [site_code=" + siteCode +
                ", pg_corp=" + selectedPgCorp.name() +
                ", payment_key=" + paymentKey +
                ", order_id=" + orderId +
                ", total_amount=" + totalAmount +
                "]";
    }
}
