package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.port.out.pg.PgWidget;
import com.wanted.clone.oneport.payments.application.service.dto.PaymentRequest;
import com.wanted.clone.oneport.payments.application.port.in.PgWidgetUseCase;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PgWidgetService implements PgWidgetUseCase {
    private final Set<PgWidget> pgWidgets;
    private final Map<PgCorp, PgWidget> pgWidgetSelector = new EnumMap<>(PgCorp.class);

    @PostConstruct
    public void init() {
        pgWidgets.forEach(pgWidget -> {
            pgWidgetSelector.put(pgWidget.provider(), pgWidget);
        });
    }

    @Override
    public String renderPgUi(PaymentRequest paymentRequest, String pageType) throws Exception {
        String pgCorpName = Optional.ofNullable(paymentRequest.getPgCorpName())
                .orElseThrow(() -> new IllegalArgumentException("PG Corp Name cannot be null"))
                .toLowerCase();

        PgWidget pgWidget = pgWidgetSelector.get(PgCorp.valueOf(pgCorpName.toUpperCase()));
        if (pgWidget == null)
            throw new IllegalArgumentException("Unsupported pgCorp name: " + pgCorpName);
        switch (pageType) {
            case "checkout":
                return pgWidget.checkout();
            case "success":
                return pgWidget.success();
            case "fail":
                return pgWidget.fail();
            default:
                throw new IllegalArgumentException("Invalid pageType name: " + pageType);
        }

    }
}
