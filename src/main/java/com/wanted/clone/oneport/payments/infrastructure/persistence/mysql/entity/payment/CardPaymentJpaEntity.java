package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment;

import com.wanted.clone.oneport.payments.domain.entity.payment.card.AcquireStatus;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter.AcquireStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "card_payment")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CardPaymentJpaEntity extends TransactionTypeJpaEntity {
    @Id
    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "approve_no")
    private String approveNo;

    @Column(name = "acquire_status")
    @Convert(converter = AcquireStatusConverter.class)
    private AcquireStatus acquireStatus;

    @Column(name = "issuer_code")
    private String issuerCode;

    @Column(name = "acquirer_code")
    private String acquirerCode;

    @Column(name = "acquirer_status")
    private String acquirerStatus;
}
