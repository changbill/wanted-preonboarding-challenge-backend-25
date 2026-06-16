package com.wanted.clone.oneport.payments.application.service.dto;

import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import lombok.Getter;

@Getter
public class PaymentRequest {
    private final PgCorp pgCorp;

    private PaymentRequest(String name) {
        this.pgCorp = PgCorp.from(name);
    }

    public static PaymentRequest of(String pgCorpName) {
        return new PaymentRequest(pgCorpName);
    }

}
