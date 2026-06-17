package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.payment.PaymentLedgerJpaEntity;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.payment.JpaPaymentLedgerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Slf4j
public class PaymentLedgerRepositoryTests {

    @Autowired
    private JpaPaymentLedgerRepository jpaPaymentLedgerRepository;

    @Test
    public void save_true_PaymentLedger() throws Exception {
        // Given
        PaymentLedgerJpaEntity paymentInfo = new PaymentLedgerJpaEntity(
                1,
                null,
                null,
                "",
                PaymentMethod.CARD,
                PaymentStatus.DONE,
                3400,
                3400,
                0,
                0
        );

        // When
        PaymentLedgerJpaEntity result = jpaPaymentLedgerRepository.save(paymentInfo);

        // Then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(paymentInfo);
    }

}
