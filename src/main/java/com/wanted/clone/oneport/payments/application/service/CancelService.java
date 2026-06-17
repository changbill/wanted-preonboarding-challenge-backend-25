package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import com.wanted.clone.oneport.payments.application.port.in.OrderCancelUseCase;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
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
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final PaymentLedgerRepository paymentLedgerRepository;

    @Transactional
    @Override
    public boolean orderCancel(CancelPaymentCommand command) throws Exception {
        String paymentKey = command.getPaymentKey();
        int cancellationAmount = command.getCancellationAmount();
        Order wantedCancelOrder = orderRepository.findByIdForUpdate(command.getOrderId());
        PaymentLedger paymentInfo = paymentService.getLatestPaymentInfoOnlyOne(paymentKey);
        PaymentAPIs paymentAPIs = paymentService.selectPgAPI(paymentInfo.getPgCorpName());

        paymentInfo.verifyCancellableAmount(cancellationAmount);
        if (command.hasItemIdx())
            wantedCancelOrder.orderCancel(command.getItemIdxs());
        else
            wantedCancelOrder.orderAllCancel();

        PaymentCancelResult response = paymentAPIs.requestPaymentCancel(paymentKey, command);
        orderRepository.save(wantedCancelOrder);
        paymentLedgerRepository.save(response.toEntity(paymentInfo.getPgCorpName()));
        return true;
    }
}
