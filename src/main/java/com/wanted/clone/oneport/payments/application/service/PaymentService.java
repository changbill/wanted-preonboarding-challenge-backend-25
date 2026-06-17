package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.application.port.in.PaymentFullfillUseCase;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
import com.wanted.clone.oneport.payments.application.port.out.repository.PaymentLedgerRepository;
import com.wanted.clone.oneport.payments.application.port.out.repository.TransactionTypeRepository;
import com.wanted.clone.oneport.payments.application.result.PaymentApprovalResult;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.domain.entity.order.OrderStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import com.wanted.clone.oneport.payments.domain.exception.UnsupportedPgCorpException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements PaymentFullfillUseCase {
    public int fee = 100;
    private final Set<PaymentAPIs> paymentAPIsSet;
    private final Set<TransactionTypeRepository> transactionTypeRepositorySet;
    private final OrderRepository orderRepository;
    private final PaymentLedgerRepository paymentLedgerRepository;

    private final Map<String, TransactionTypeRepository> transactionTypeRepositories = new HashMap<>();
    private final Map<PgCorp, PaymentAPIs> pgAPIs = new EnumMap<>(PgCorp.class);

    @PostConstruct
    public void init() {
        for (PaymentAPIs paymentAPI : paymentAPIsSet) {
            pgAPIs.put(paymentAPI.provider(), paymentAPI);
        }

        for (TransactionTypeRepository transactionTypeRepository : transactionTypeRepositorySet) {
            String paymentMethodType = transactionTypeRepository.getClass().getSimpleName().split("TransactionTypeRepository")[0].toLowerCase();
            transactionTypeRepositories.put(paymentMethodType, transactionTypeRepository);
        }
    }

    @Transactional
    @Override
    public String paymentApproved(ApprovePaymentCommand command) throws IOException {
        String orderId = command.getOrderId();
        Order order = orderRepository.findById(orderId);
        if (order.isPaidWith(command.getPaymentKey())) {
            return "success";
        }
        verifyOrderIsPayable(order, command);
        PaymentAPIs paymentAPIs = selectPgAPI(command.getSelectedPgCorp());
        PaymentApprovalResult response = paymentAPIs.requestPaymentApprove(command);

        if (paymentAPIs.isPaymentApproved(response.getStatus().name())) {
            order.orderPaymentFullFill(response.getTransactionId());
            paymentLedgerRepository.save(response.toEntity(command.getSelectedPgCorp()));

            return "success";
        }

        return "fail";
    }

    public PaymentLedger getLatestPaymentInfoOnlyOne(String paymentKey) {
        return paymentLedgerRepository.findOneByTransactionIdDesc(paymentKey);
    }

    public PaymentAPIs selectPgAPI(PgCorp pgCorp) {
        PaymentAPIs paymentAPIs = pgAPIs.get(pgCorp);
        if (paymentAPIs == null)
            throw UnsupportedPgCorpException.forProvider(pgCorp);
        return paymentAPIs;
    }

    private void verifyOrderIsPayable(Order order, ApprovePaymentCommand command) {
        if (order.isPaymentFulfilled()) {
            throw PaymentRuleViolationException.alreadyPaidOrder(command.getOrderId());
        }
        if (!order.getStatus().equals(OrderStatus.ORDER_COMPLETED)) {
            throw PaymentRuleViolationException.notPayableOrder(command.getOrderId());
        }
        if (paymentLedgerRepository.existsByTransactionIdAndPaymentStatus(command.getPaymentKey(), PaymentStatus.DONE)) {
            throw PaymentRuleViolationException.duplicatedPaymentKey(command.getPaymentKey());
        }
    }

}
