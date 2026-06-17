package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.payment;

import com.wanted.clone.oneport.payments.application.port.out.repository.TransactionTypeRepository;
import com.wanted.clone.oneport.payments.domain.entity.payment.TransactionType;
import com.wanted.clone.oneport.payments.domain.entity.payment.card.CardPayment;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.mapper.PaymentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.NoSuchElementException;

@Repository
@RequiredArgsConstructor
public class CardTransactionTypeRepository implements TransactionTypeRepository {
    private final JpaCardPaymentRepository jpaCardPaymentRepository;

    @Override
    public CardPayment findById(String paymentKey) {
        return jpaCardPaymentRepository.findById(paymentKey)
            .map(PaymentPersistenceMapper::toDomain)
            .orElseThrow(() -> new NoSuchElementException(String.format("CardPayment with key '%s' not found", paymentKey)));
    }

    @Override
    public void save(TransactionType paymentDetailInfo) {
        jpaCardPaymentRepository.save(PaymentPersistenceMapper.toJpaCardPayment(paymentDetailInfo));
    }
}
