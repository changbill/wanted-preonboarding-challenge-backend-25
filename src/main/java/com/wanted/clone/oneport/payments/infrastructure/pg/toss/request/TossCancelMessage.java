package com.wanted.clone.oneport.payments.infrastructure.pg.toss.request;

import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossCancelMessage {
    private final String cancelReason;
    private final int cancelAmount;

    public static TossCancelMessage from(CancelPaymentCommand requestMessage) {
        return TossCancelMessage.builder()
                .cancelReason(requestMessage.getCancelReason())
                .cancelAmount(requestMessage.getCancellationAmount())
                .build();
    }
}
