package com.wanted.clone.oneport.payments.infrastructure.pg.toss;

import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.result.PaymentApprovalResult;
import com.wanted.clone.oneport.payments.application.result.PaymentCancelResult;
import com.wanted.clone.oneport.payments.application.result.PaymentSettlementResult;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.request.TossApproveMessage;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.request.TossCancelMessage;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.TossApproveResponseMessage;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.TossCancelResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TossPayment implements PaymentAPIs {
    private final TossPaymentAPIs tossClient;

    @Override
    public PgCorp provider() {
        return PgCorp.TOSS;
    }

    @Override
    public PaymentApprovalResult requestPaymentApprove(ApprovePaymentCommand requestMessage) throws IOException {
        TossApproveMessage message = TossApproveMessage.from(requestMessage);
        Response<TossApproveResponseMessage> response = tossClient.paymentFullfill(message).execute();
        if (response.isSuccessful())
            return Objects.requireNonNull(response.body())
                    .toCommonMessage();

        throw new IOException(response.message());
    }

    @Override
    public boolean isPaymentApproved(String status) {
        return "DONE".equalsIgnoreCase(status);
    }

    @Override
    public PaymentCancelResult requestPaymentCancel(String txId, CancelPaymentCommand requestMessage) throws IOException {
        Response<TossCancelResponseMessage> response = tossClient.paymentCancel(txId, TossCancelMessage.from(requestMessage)).execute();
        if (response.isSuccessful()) {
            return Objects.requireNonNull(response.body()).toCommonMessage();
        }

        throw new IOException(response.message());
    }

    @Override
    public List<PaymentSettlementResult> requestPaymentSettlement() throws IOException {
        return null;
    }
}
