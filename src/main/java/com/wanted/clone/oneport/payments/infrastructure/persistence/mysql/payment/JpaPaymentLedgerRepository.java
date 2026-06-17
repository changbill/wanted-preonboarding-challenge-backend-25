package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.payment;


import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.JpaBaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPaymentLedgerRepository extends JpaBaseRepository<PaymentLedger, String> {
    Optional<List<PaymentLedger>> findByTransactionId(String paymentKey);

    Optional<PaymentLedger> findTopByTransactionIdOrderByIdDesc(String paymentKey);

    boolean existsByTransactionIdAndPaymentStatus(String paymentKey, PaymentStatus paymentStatus);
}
