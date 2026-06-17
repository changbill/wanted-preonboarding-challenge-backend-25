package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.mapper;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.TransactionType;
import com.wanted.clone.oneport.payments.domain.entity.payment.card.CardPayment;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment.CardPaymentJpaEntity;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment.PaymentLedgerJpaEntity;

public class PaymentPersistenceMapper {
    private PaymentPersistenceMapper() {
    }

    public static PaymentLedgerJpaEntity toJpaEntity(PaymentLedger ledger) {
        return new PaymentLedgerJpaEntity(
                ledger.getId(),
                ledger.getSiteCode(),
                ledger.getPgCorpName(),
                ledger.getTransactionId(),
                ledger.getMethod(),
                ledger.getPaymentStatus(),
                ledger.getTotalAmount(),
                ledger.getBalanceAmount(),
                ledger.getCanceledAmount(),
                ledger.getPayOutAmount()
        );
    }

    public static PaymentLedger toDomain(PaymentLedgerJpaEntity entity) {
        return PaymentLedger.builder()
                .id(entity.getId())
                .siteCode(entity.getSiteCode())
                .pgCorpName(entity.getPgCorpName())
                .transactionId(entity.getTransactionId())
                .method(entity.getMethod())
                .paymentStatus(entity.getPaymentStatus())
                .totalAmount(entity.getTotalAmount())
                .balanceAmount(entity.getBalanceAmount())
                .canceledAmount(entity.getCanceledAmount())
                .payOutAmount(entity.getPayOutAmount())
                .build();
    }

    public static CardPaymentJpaEntity toJpaCardPayment(TransactionType transactionType) {
        CardPayment cardPayment = (CardPayment) transactionType;
        return CardPaymentJpaEntity.builder()
                .paymentKey(cardPayment.getPaymentKey())
                .cardNumber(cardPayment.getCardNumber())
                .approveNo(cardPayment.getApproveNo())
                .acquireStatus(cardPayment.getAcquireStatus())
                .issuerCode(cardPayment.getIssuer_code())
                .acquirerCode(cardPayment.getAcquirerCode())
                .acquirerStatus(cardPayment.getAcquirerStatus())
                .build();
    }

    public static CardPayment toDomain(CardPaymentJpaEntity entity) {
        return CardPayment.builder()
                .paymentKey(entity.getPaymentKey())
                .cardNumber(entity.getCardNumber())
                .approveNo(entity.getApproveNo())
                .acquireStatus(entity.getAcquireStatus())
                .issuer_code(entity.getIssuerCode())
                .acquirerCode(entity.getAcquirerCode())
                .acquirerStatus(entity.getAcquirerStatus())
                .build();
    }
}
