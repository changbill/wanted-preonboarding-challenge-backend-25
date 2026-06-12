package com.wanted.clone.oneport.payments.application.port.in;

import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;

import java.io.IOException;

public interface PaymentFullfillUseCase {
    String paymentApproved(ApprovePaymentCommand command) throws IOException;
}
