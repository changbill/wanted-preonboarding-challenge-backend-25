package com.wanted.clone.oneport.payments.domain.entity.payment.card;

import com.wanted.clone.oneport.payments.domain.entity.payment.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class CardPayment extends TransactionType {
    private String paymentKey; // example) tgen_20240605132741Jtkz1

    private String cardNumber;

    private String approveNo;

    private AcquireStatus acquireStatus;

    private String issuer_code;

    private String acquirerCode;

    private String acquirerStatus;

    protected CardPayment() {
    }

    public static CardPayment from(String paymentKey,
                                   String cardNumber,
                                   String approveNo,
                                   String acquireStatus,
                                   String acquirerCode) {
        return CardPayment.builder()
                .paymentKey(paymentKey)
                .cardNumber(cardNumber)
                .approveNo(approveNo)
                .acquireStatus(AcquireStatus.valueOf(acquireStatus))
                .acquirerCode(acquirerCode)
                .acquirerStatus(acquireStatus)
                .build();
    }
}
