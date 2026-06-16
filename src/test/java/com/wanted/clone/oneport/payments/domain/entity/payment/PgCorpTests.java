package com.wanted.clone.oneport.payments.domain.entity.payment;

import com.wanted.clone.oneport.payments.domain.exception.UnsupportedPgCorpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PgCorpTests {

    @Test
    void from_LowercaseName_ReturnsPgCorp() {
        Assertions.assertEquals(PgCorp.TOSS, PgCorp.from("toss"));
    }

    @Test
    void from_HyphenatedName_ReturnsPgCorp() {
        Assertions.assertEquals(PgCorp.NHN_KCP, PgCorp.from("nhn-kcp"));
    }

    @Test
    void from_UnsupportedName_ThrowsException() {
        Assertions.assertThrows(
            UnsupportedPgCorpException.class,
            () -> PgCorp.from("unknown")
        );
    }
}
