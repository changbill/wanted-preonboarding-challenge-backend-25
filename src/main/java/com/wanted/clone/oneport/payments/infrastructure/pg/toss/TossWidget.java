package com.wanted.clone.oneport.payments.infrastructure.pg.toss;

import com.wanted.clone.oneport.payments.application.port.out.pg.PgWidget;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import org.springframework.stereotype.Component;

@Component
public class TossWidget implements PgWidget {
    @Override
    public PgCorp provider() {
        return PgCorp.TOSS;
    }

    @Override
    public String checkout() {
        return "toss/checkout";
    }

    @Override
    public String success() {
        return "toss/success";
    }

    @Override
    public String fail() {
        return "toss/fail";
    }
}
