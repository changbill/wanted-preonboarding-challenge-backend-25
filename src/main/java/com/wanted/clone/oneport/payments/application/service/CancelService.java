package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import com.wanted.clone.oneport.payments.application.port.in.OrderCancelUseCase;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.port.out.repository.PaymentLedgerRepository;
import com.wanted.clone.oneport.payments.application.result.PaymentCancelResult;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelService implements OrderCancelUseCase {
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentLedgerRepository paymentLedgerRepository;

    @Transactional
    @Override
    public boolean orderCancel(CancelPaymentCommand command) throws Exception {
        String paymentKey = command.getPaymentKey();
        int cancellationAmount = command.getCancellationAmount();
        Order wantedCancelOrder = orderService.getOrderInfo(command.getOrderId());
        PaymentLedger paymentInfo = paymentService.getLatestPaymentInfoOnlyOne(paymentKey);
        PaymentAPIs paymentAPIs = paymentService.selectPgAPI(paymentInfo.getPgCorpName());

        if (wantedCancelOrder.isNotOrderStatusPurchaseDecision() &&
                paymentInfo.isCancellableAmountGreaterThan(cancellationAmount)) {
            if (command.hasItemIdx())
                wantedCancelOrder.orderCancel(command.getItemIdxs());
            else
                wantedCancelOrder.orderAllCancel();
            PaymentCancelResult response = paymentAPIs.requestPaymentCancel(paymentKey, command);
            paymentLedgerRepository.save(response.toEntity());
            return true;
        }

        throw new Exception("Not Enough CancellationAmount");
    }
}
