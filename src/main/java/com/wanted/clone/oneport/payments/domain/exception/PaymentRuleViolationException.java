package com.wanted.clone.oneport.payments.domain.exception;

public class PaymentRuleViolationException extends RuntimeException {

    private PaymentRuleViolationException(String message) {
        super(message);
    }

    public static PaymentRuleViolationException alreadyPaidOrder(String orderId) {
        return new PaymentRuleViolationException("Order is already paid: " + orderId);
    }

    public static PaymentRuleViolationException duplicatedPaymentKey(String paymentKey) {
        return new PaymentRuleViolationException("Payment key already exists: " + paymentKey);
    }

    public static PaymentRuleViolationException notPayableOrder(String orderId) {
        return new PaymentRuleViolationException("Order is not payable: " + orderId);
    }

    public static PaymentRuleViolationException purchaseDecisionOrder(String orderId) {
        return new PaymentRuleViolationException("Purchase decision order cannot be canceled: " + orderId);
    }

    public static PaymentRuleViolationException insufficientCancellableAmount(int cancellationAmount, int balanceAmount) {
        return new PaymentRuleViolationException(
            "Cancellation amount " + cancellationAmount + " exceeds balance amount " + balanceAmount
        );
    }

    public static PaymentRuleViolationException orderItemNotFound(int itemIdx) {
        return new PaymentRuleViolationException("Order item not found: " + itemIdx);
    }
}
