package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.payment;


import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.JpaBaseRepository;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment.PaymentLedgerJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPaymentLedgerRepository extends JpaBaseRepository<PaymentLedgerJpaEntity, String> {
    Optional<List<PaymentLedgerJpaEntity>> findByTransactionId(String paymentKey);

    Optional<PaymentLedgerJpaEntity> findTopByTransactionIdOrderByIdDesc(String paymentKey);

    boolean existsByTransactionIdAndPaymentStatus(String paymentKey, PaymentStatus paymentStatus);
}
