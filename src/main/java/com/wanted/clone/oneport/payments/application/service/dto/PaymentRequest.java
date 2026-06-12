package com.wanted.clone.oneport.payments.application.service.dto;

import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import lombok.Getter;

@Getter
public class PaymentRequest {
    private final String pgCorpName;

    private PaymentRequest(String name) {
        this.pgCorpName = PgCorp.valueOf(name.toUpperCase()).toString().toLowerCase();
    }

    public static PaymentRequest of(String pgCorpName) {
        return new PaymentRequest(pgCorpName);
    }

}
