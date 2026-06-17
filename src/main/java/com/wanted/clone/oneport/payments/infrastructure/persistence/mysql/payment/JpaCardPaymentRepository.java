package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.payment;


import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.JpaBaseRepository;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment.CardPaymentJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCardPaymentRepository extends JpaBaseRepository<CardPaymentJpaEntity, String> {
}
