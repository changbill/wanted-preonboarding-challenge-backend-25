package com.wanted.clone.oneport.payments.infrastructure.pg.toss.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wanted.clone.oneport.payments.application.result.PaymentApprovalResult;
import com.wanted.clone.oneport.payments.domain.entity.payment.TransactionType;
import com.wanted.clone.oneport.payments.domain.entity.payment.card.CardPayment;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.payment.TossCommonResponseMessage;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.payment.method.Card;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossApproveResponseMessage extends TossCommonResponseMessage {
    private String orderName;
    private Card card;
    private String lastTransactionKey;
    private int suppliedAmount; // 공급 가액
    private int vat;
    private String requestedAt; // 2024-06-18T15:13:15+09:00
    private String approvedAt;

    public PaymentApprovalResult toCommonMessage() {
        return PaymentApprovalResult.builder()
                .transactionId(this.getPaymentKey())
                .method(PaymentMethod.fromMethodName(this.getMethod()))
                .status(PaymentStatus.valueOf(this.getStatus()))
                .totalAmount(this.getTotalAmount())
                .balanceAmount(this.getBalanceAmount())
                .build();
    }

    public TransactionType toTransactionType() {
        return switch (this.getMethod()) {
            case "카드" -> CardPayment.from(
                this.getPaymentKey(),
                this.getCard().getNumber(),
                this.getCard().getApproveNo(),
                this.getCard().getAcquireStatus(),
                this.getCard().getAcquirerCode()
            );
            default -> throw new RuntimeException("Unsupported TransactionType method ::: " + this.getMethod());
        };
    }
}
