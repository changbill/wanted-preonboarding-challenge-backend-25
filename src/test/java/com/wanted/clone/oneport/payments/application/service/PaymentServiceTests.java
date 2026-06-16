package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
import com.wanted.clone.oneport.payments.application.port.out.repository.PaymentLedgerRepository;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentServiceTests {

    @Test
    void selectPgAPI_TOSS_ReturnsProviderAdapter() {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);

        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            mock(OrderRepository.class),
            mock(PaymentLedgerRepository.class)
        );
        paymentService.init();

        PaymentAPIs selectedPayment = paymentService.selectPgAPI(PgCorp.TOSS);

        Assertions.assertSame(tossPayment, selectedPayment);
    }

    @Test
    void selectPgAPI_UnsupportedProvider_ThrowsException() {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);

        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            mock(OrderRepository.class),
            mock(PaymentLedgerRepository.class)
        );
        paymentService.init();

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.selectPgAPI(PgCorp.NHN_KCP)
        );
    }
}
