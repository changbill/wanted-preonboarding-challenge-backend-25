package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.payment;

import com.wanted.clone.oneport.payments.application.port.out.repository.PaymentLedgerRepository;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentLedger;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentTransactionLedgerRepository implements PaymentLedgerRepository {
    private final JdbcTemplate jdbcTemplate;
    private final JpaPaymentLedgerRepository jpaPaymentLedgerRepository;

    @Override
    public List<PaymentLedger> findAllByTransactionId(String paymentKey) {
        return jpaPaymentLedgerRepository.findByTransactionId(paymentKey)
            .orElseThrow(() -> new NullPointerException("findAllByPaymentKey ::: Not found Payment Transactions"));
    }

    @Override
    public PaymentLedger findOneByTransactionIdDesc(String paymentKey) {
        return jpaPaymentLedgerRepository.findTopByTransactionIdOrderByIdDesc(paymentKey)
            .orElseThrow(() -> new NullPointerException("findOneByPaymentKeyDesc ::: Not found Payment Transaction"));
    }

    @Override
    public boolean existsByTransactionIdAndPaymentStatus(String paymentKey, PaymentStatus paymentStatus) {
        return jpaPaymentLedgerRepository.existsByTransactionIdAndPaymentStatus(paymentKey, paymentStatus);
    }

    @Override
    public void save(PaymentLedger paymentLedgerInfo) {
        jpaPaymentLedgerRepository.save(paymentLedgerInfo);
    }

    @Override
    public void bulkInsert(List<PaymentLedger> paymentLedgerHistories) {
        String sqlStatement = "INSERT INTO payment_ledger (site_code, tx_id, pg_corp, method, payment_Status, total_amount, balance_amount, canceled_amount, pay_out_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sqlStatement,
                paymentLedgerHistories,
                paymentLedgerHistories.size(),
                (PreparedStatement ps, PaymentLedger data) -> {
                    ps.setString(1, data.getSiteCode());
                    ps.setString(2, data.getTransactionId());
                    ps.setInt(3, data.getPgCorpName().getCode());
                    ps.setString(4, data.getMethod().toString());
                    ps.setString(5, data.getPaymentStatus().toString());
                    ps.setInt(6, data.getTotalAmount());
                    ps.setInt(7, data.getBalanceAmount());
                    ps.setInt(8, data.getCanceledAmount());
                    ps.setInt(9, data.getPayOutAmount());
                });
    }
}
