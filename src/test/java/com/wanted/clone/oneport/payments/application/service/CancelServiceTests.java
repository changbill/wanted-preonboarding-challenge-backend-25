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
import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Test
    void orderCancel_InsufficientAmount_DoesNotCallPgOrSave() throws Exception {
        CancelPaymentCommand command = new CancelPaymentCommand(
            "order-1",
            null,
            "cancel",
            "payment-key-1",
            2000
        );
        Order order = new Order("order-1", "buyer", "010", List.of());
        order.orderPaymentFullFill("payment-key-1");

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1")).thenReturn(order);

        PaymentLedger paymentLedger = paymentLedger(1000);
        PaymentService paymentService = mock(PaymentService.class);
        when(paymentService.getLatestPaymentInfoOnlyOne("payment-key-1")).thenReturn(paymentLedger);

        PaymentAPIs paymentAPIs = mock(PaymentAPIs.class);
        when(paymentService.selectPgAPI(PgCorp.TOSS)).thenReturn(paymentAPIs);

        PaymentLedgerRepository paymentLedgerRepository = mock(PaymentLedgerRepository.class);
        CancelService cancelService = new CancelService(orderRepository, paymentService, paymentLedgerRepository);

        Assertions.assertThrows(
            PaymentRuleViolationException.class,
            () -> cancelService.orderCancel(command)
        );
        verify(paymentAPIs, never()).requestPaymentCancel(any(), any());
        verify(orderRepository, never()).save(any());
        verify(paymentLedgerRepository, never()).save(any());
    }

    @Test
    void orderCancel_LockFailure_PropagatesBeforePgRequest() throws Exception {
        CancelPaymentCommand command = new CancelPaymentCommand(
            "order-1",
            null,
            "cancel",
            "payment-key-1",
            1000
        );
        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1"))
            .thenThrow(new CannotAcquireLockException("lock wait timeout"));

        PaymentService paymentService = mock(PaymentService.class);
        PaymentLedgerRepository paymentLedgerRepository = mock(PaymentLedgerRepository.class);
        CancelService cancelService = new CancelService(orderRepository, paymentService, paymentLedgerRepository);

        Assertions.assertThrows(
            CannotAcquireLockException.class,
            () -> cancelService.orderCancel(command)
        );
        verify(paymentService, never()).getLatestPaymentInfoOnlyOne(any());
        verify(paymentLedgerRepository, never()).save(any());
    }

    private PaymentLedger paymentLedger(int balanceAmount) {
        return PaymentLedger.builder()
            .transactionId("payment-key-1")
            .pgCorpName(PgCorp.TOSS)
            .method(PaymentMethod.CARD)
            .paymentStatus(PaymentStatus.DONE)
            .totalAmount(1000)
            .balanceAmount(balanceAmount)
            .canceledAmount(0)
            .build();
    }
}
