package com.wanted.clone.oneport.payments.infrastructure.pg;

import java.io.IOException;

public class PgPaymentGatewayException extends IOException {
    private final int statusCode;
    private final String errorCode;

    public PgPaymentGatewayException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
