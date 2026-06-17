package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.mapper;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.domain.entity.payment.card.AcquireStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.card.CardPayment;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment.CardPaymentJpaEntity;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment.PaymentLedgerJpaEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentPersistenceMapperTests {
    @Test
    void maps_payment_ledger_between_domain_and_jpa_entity() {
        PaymentLedger ledger = PaymentLedger.builder()
                .id(10)
                .siteCode("site")
                .pgCorpName(PgCorp.TOSS)
                .transactionId("tx-1")
                .method(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.DONE)
                .totalAmount(1000)
                .balanceAmount(1000)
                .canceledAmount(0)
                .payOutAmount(900)
                .build();

        PaymentLedgerJpaEntity jpaEntity = PaymentPersistenceMapper.toJpaEntity(ledger);
        PaymentLedger mapped = PaymentPersistenceMapper.toDomain(jpaEntity);

        assertThat(mapped).usingRecursiveComparison().isEqualTo(ledger);
    }

    @Test
    void maps_card_payment_between_domain_and_jpa_entity() {
        CardPayment cardPayment = CardPayment.builder()
                .paymentKey("payment-key")
                .cardNumber("1234")
                .approveNo("approve")
                .acquireStatus(AcquireStatus.READY)
                .issuer_code("issuer")
                .acquirerCode("acquirer")
                .acquirerStatus("READY")
                .build();

        CardPaymentJpaEntity jpaEntity = PaymentPersistenceMapper.toJpaCardPayment(cardPayment);
        CardPayment mapped = PaymentPersistenceMapper.toDomain(jpaEntity);

        assertThat(mapped).usingRecursiveComparison().isEqualTo(cardPayment);
    }
}
