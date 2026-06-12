package com.wanted.clone.oneport.payments.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancelPaymentCommand {
    private String orderId;
    private int[] itemIdxs;
    private String cancelReason;
    private String paymentKey;
    private int cancellationAmount;

    public boolean hasItemIdx() {
        return this.getItemIdxs() != null && this.getItemIdxs().length > 0;
    }
}
