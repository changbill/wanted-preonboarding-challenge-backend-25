package com.wanted.clone.oneport.payments.domain.entity.payment;

import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentLedgerTests {

    @Test
    void verifyCancellableAmount_LessThanBalance_DoesNotThrow() {
        PaymentLedger ledger = PaymentLedger.builder()
            .balanceAmount(1000)
            .build();

        Assertions.assertDoesNotThrow(() -> ledger.verifyCancellableAmount(1000));
    }

    @Test
    void verifyCancellableAmount_GreaterThanBalance_ThrowsException() {
        PaymentLedger ledger = PaymentLedger.builder()
            .balanceAmount(1000)
            .build();

        Assertions.assertThrows(
            PaymentRuleViolationException.class,
            () -> ledger.verifyCancellableAmount(1001)
        );
    }
}
