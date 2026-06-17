package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter.PaymentMethodConverter;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter.PaymentStatusConverter;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter.PgCorpConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "payment_ledger",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_payment_ledger_tx_method_status",
        columnNames = {"tx_id", "method", "payment_status"}
    )
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentLedgerJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "site_code")
    private String siteCode;

    @Column(name = "pg_corp")
    @Convert(converter = PgCorpConverter.class)
    private PgCorp pgCorpName;

    @Column(name = "tx_id")
    private String transactionId;

    @Convert(converter = PaymentMethodConverter.class)
    private PaymentMethod method;

    @Column(name = "payment_status")
    @Convert(converter = PaymentStatusConverter.class)
    private PaymentStatus paymentStatus;

    @Column(name = "total_amount")
    private int totalAmount;

    @Column(name = "balance_amount")
    private int balanceAmount;

    @Column(name = "canceled_amount")
    private int canceledAmount;

    @Column(name = "pay_out_amount")
    private int payOutAmount;
}
