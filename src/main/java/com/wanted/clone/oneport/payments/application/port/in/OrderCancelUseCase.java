package com.wanted.clone.oneport.payments.application.port.in;

import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;

public interface OrderCancelUseCase {
    boolean orderCancel(CancelPaymentCommand command) throws Exception;
}
