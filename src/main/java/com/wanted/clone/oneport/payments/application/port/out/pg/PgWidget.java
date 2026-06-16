package com.wanted.clone.oneport.payments.application.port.out.pg;

import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;

public interface PgWidget {
    PgCorp provider();

    String checkout();

    String success();

    String fail();
}
