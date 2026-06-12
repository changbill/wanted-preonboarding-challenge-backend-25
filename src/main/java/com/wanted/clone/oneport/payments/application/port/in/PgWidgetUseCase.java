package com.wanted.clone.oneport.payments.application.port.in;

import com.wanted.clone.oneport.payments.application.service.dto.PaymentRequest;

public interface PgWidgetUseCase {
    String renderPgUi(PaymentRequest paymentRequest, String pageName) throws Exception;
}
