package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.port.out.pg.PgWidget;
import com.wanted.clone.oneport.payments.application.service.dto.PaymentRequest;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PgWidgetServiceTests {

    @Test
    void renderPgUi_Checkout_ReturnsProviderCheckoutTemplate() throws Exception {
        PgWidget tossWidget = mock(PgWidget.class);
        when(tossWidget.provider()).thenReturn(PgCorp.TOSS);
        when(tossWidget.checkout()).thenReturn("toss/checkout");

        PgWidgetService pgWidgetService = new PgWidgetService(Set.of(tossWidget));
        pgWidgetService.init();

        String template = pgWidgetService.renderPgUi(PaymentRequest.of("toss"), "checkout");

        Assertions.assertEquals("toss/checkout", template);
    }

    @Test
    void renderPgUi_UnsupportedProvider_ThrowsException() {
        PgWidget tossWidget = mock(PgWidget.class);
        when(tossWidget.provider()).thenReturn(PgCorp.TOSS);

        PgWidgetService pgWidgetService = new PgWidgetService(Set.of(tossWidget));
        pgWidgetService.init();

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> pgWidgetService.renderPgUi(PaymentRequest.of("nhn_kcp"), "checkout")
        );
    }
}
