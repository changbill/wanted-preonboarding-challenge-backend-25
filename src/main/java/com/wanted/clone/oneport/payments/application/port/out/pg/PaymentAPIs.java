package com.wanted.clone.oneport.payments.application.port.out.pg;

import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import com.wanted.clone.oneport.payments.application.result.PaymentApprovalResult;
import com.wanted.clone.oneport.payments.application.result.PaymentCancelResult;
import com.wanted.clone.oneport.payments.application.result.PaymentSettlementResult;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;

import java.io.IOException;
import java.util.List;

public interface PaymentAPIs {
    PgCorp provider();

    PaymentApprovalResult requestPaymentApprove(ApprovePaymentCommand command) throws IOException;

    boolean isPaymentApproved(String status);

    PaymentCancelResult requestPaymentCancel(String txId, CancelPaymentCommand command) throws IOException;

    List<PaymentSettlementResult> requestPaymentSettlement() throws IOException;
}
