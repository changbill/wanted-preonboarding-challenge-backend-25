package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
import com.wanted.clone.oneport.payments.application.port.out.repository.PaymentLedgerRepository;
import com.wanted.clone.oneport.payments.application.result.PaymentCancelResult;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancelServiceTests {

    @Test
    void orderCancel_UsesLockedOrderAndSavesOrder() throws Exception {
        CancelPaymentCommand command = new CancelPaymentCommand(
            "order-1",
            null,
            "cancel",
            "payment-key-1",
            1000
        );
        Order order = new Order("order-1", "buyer", "010", List.of());
        order.orderPaymentFullFill("payment-key-1");

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1")).thenReturn(order);

        PaymentLedger paymentLedger = PaymentLedger.builder()
            .transactionId("payment-key-1")
            .pgCorpName(PgCorp.TOSS)
            .method(PaymentMethod.CARD)
            .paymentStatus(PaymentStatus.DONE)
            .totalAmount(1000)
            .balanceAmount(1000)
            .canceledAmount(0)
            .build();

        PaymentService paymentService = mock(PaymentService.class);
        when(paymentService.getLatestPaymentInfoOnlyOne("payment-key-1")).thenReturn(paymentLedger);

        PaymentAPIs paymentAPIs = mock(PaymentAPIs.class);
        when(paymentService.selectPgAPI(PgCorp.TOSS)).thenReturn(paymentAPIs);
        when(paymentAPIs.requestPaymentCancel("payment-key-1", command))
            .thenReturn(PaymentCancelResult.builder()
                .transactionId("payment-key-1")
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.CANCELED)
                .totalAmount(1000)
                .balanceAmount(0)
                .canceledAmount(1000)
                .build());

        PaymentLedgerRepository paymentLedgerRepository = mock(PaymentLedgerRepository.class);
        CancelService cancelService = new CancelService(orderRepository, paymentService, paymentLedgerRepository);

        boolean result = cancelService.orderCancel(command);

        Assertions.assertTrue(result);
        verify(orderRepository).findByIdForUpdate("order-1");
        verify(orderRepository).save(order);
        verify(paymentLedgerRepository).save(any());
    }
}
