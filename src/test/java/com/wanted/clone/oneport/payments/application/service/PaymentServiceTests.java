package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
import com.wanted.clone.oneport.payments.application.port.out.repository.PaymentLedgerRepository;
import com.wanted.clone.oneport.payments.application.result.PaymentApprovalResult;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import com.wanted.clone.oneport.payments.domain.exception.UnsupportedPgCorpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
            UnsupportedPgCorpException.class,
            () -> paymentService.selectPgAPI(PgCorp.NHN_KCP)
        );
    }

    @Test
    void paymentApproved_SameOrderAndPaymentKey_ReturnsSuccessWithoutPgRequest() throws Exception {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);

        Order order = new Order("order-1", "buyer", "010", List.of());
        order.orderPaymentFullFill("payment-key-1");

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1")).thenReturn(order);

        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            orderRepository,
            mock(PaymentLedgerRepository.class)
        );
        paymentService.init();

        String result = paymentService.paymentApproved(approveCommand("order-1", "payment-key-1"));

        Assertions.assertEquals("success", result);
        verify(tossPayment, never()).requestPaymentApprove(any());
    }

    @Test
    void paymentApproved_AlreadyPaidOrderWithDifferentPaymentKey_ThrowsException() throws Exception {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);

        Order order = new Order("order-1", "buyer", "010", List.of());
        order.orderPaymentFullFill("payment-key-1");

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1")).thenReturn(order);

        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            orderRepository,
            mock(PaymentLedgerRepository.class)
        );
        paymentService.init();

        Assertions.assertThrows(
            PaymentRuleViolationException.class,
            () -> paymentService.paymentApproved(approveCommand("order-1", "payment-key-2"))
        );
    }

    @Test
    void paymentApproved_DuplicatedPaymentKey_ThrowsExceptionBeforePgRequest() throws Exception {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1"))
            .thenReturn(new Order("order-1", "buyer", "010", List.of()));

        PaymentLedgerRepository paymentLedgerRepository = mock(PaymentLedgerRepository.class);
        when(paymentLedgerRepository.existsByTransactionIdAndPaymentStatus("payment-key-1", PaymentStatus.DONE))
            .thenReturn(true);

        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            orderRepository,
            paymentLedgerRepository
        );
        paymentService.init();

        Assertions.assertThrows(
            PaymentRuleViolationException.class,
            () -> paymentService.paymentApproved(approveCommand("order-1", "payment-key-1"))
        );
        verify(tossPayment, never()).requestPaymentApprove(any());
    }

    @Test
    void paymentApproved_NewApproval_SavesLedgerOnce() throws Exception {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);
        when(tossPayment.requestPaymentApprove(any()))
            .thenReturn(PaymentApprovalResult.builder()
                .transactionId("payment-key-1")
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.DONE)
                .totalAmount(1000)
                .balanceAmount(1000)
                .build());
        when(tossPayment.isPaymentApproved("DONE")).thenReturn(true);

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1"))
            .thenReturn(new Order("order-1", "buyer", "010", List.of()));

        PaymentLedgerRepository paymentLedgerRepository = mock(PaymentLedgerRepository.class);

        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            orderRepository,
            paymentLedgerRepository
        );
        paymentService.init();

        String result = paymentService.paymentApproved(approveCommand("order-1", "payment-key-1"));

        Assertions.assertEquals("success", result);
        verify(orderRepository).findByIdForUpdate("order-1");
        verify(orderRepository).save(any());
        verify(paymentLedgerRepository).save(any());
    }

    @Test
    void paymentApproved_PgReturnsNotApproved_DoesNotSaveOrderOrLedger() throws Exception {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);
        when(tossPayment.requestPaymentApprove(any()))
            .thenReturn(PaymentApprovalResult.builder()
                .transactionId("payment-key-1")
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.PARTIAL_CANCELED)
                .totalAmount(1000)
                .balanceAmount(1000)
                .build());
        when(tossPayment.isPaymentApproved("PARTIAL_CANCELED")).thenReturn(false);

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1"))
            .thenReturn(new Order("order-1", "buyer", "010", List.of()));

        PaymentLedgerRepository paymentLedgerRepository = mock(PaymentLedgerRepository.class);
        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            orderRepository,
            paymentLedgerRepository
        );
        paymentService.init();

        String result = paymentService.paymentApproved(approveCommand("order-1", "payment-key-1"));

        Assertions.assertEquals("fail", result);
        verify(orderRepository, never()).save(any());
        verify(paymentLedgerRepository, never()).save(any());
    }

    @Test
    void paymentApproved_LockFailure_PropagatesBeforePgRequest() throws Exception {
        PaymentAPIs tossPayment = mock(PaymentAPIs.class);
        when(tossPayment.provider()).thenReturn(PgCorp.TOSS);

        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByIdForUpdate("order-1"))
            .thenThrow(new CannotAcquireLockException("lock wait timeout"));

        PaymentService paymentService = new PaymentService(
            Set.of(tossPayment),
            Set.of(),
            orderRepository,
            mock(PaymentLedgerRepository.class)
        );
        paymentService.init();

        Assertions.assertThrows(
            CannotAcquireLockException.class,
            () -> paymentService.paymentApproved(approveCommand("order-1", "payment-key-1"))
        );
        verify(tossPayment, never()).requestPaymentApprove(any());
    }

    private ApprovePaymentCommand approveCommand(String orderId, String paymentKey) {
        return ApprovePaymentCommand.builder()
            .orderId(orderId)
            .paymentKey(paymentKey)
            .selectedPgCorp(PgCorp.TOSS)
            .totalAmount(1000)
            .build();
    }
}
