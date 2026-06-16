package com.wanted.clone.oneport.payments.domain.exception;

import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;

public class UnsupportedPgCorpException extends RuntimeException {

    private UnsupportedPgCorpException(String message) {
        super(message);
    }

    public static UnsupportedPgCorpException forName(String pgCorpName) {
        return new UnsupportedPgCorpException("Unsupported pgCorp name: " + pgCorpName);
    }

    public static UnsupportedPgCorpException forProvider(PgCorp pgCorp) {
        return new UnsupportedPgCorpException("Unsupported pgCorp: " + pgCorp);
    }
}
