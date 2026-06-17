package com.wanted.clone.oneport.payments.infrastructure.pg.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import com.wanted.clone.oneport.payments.application.result.PaymentApprovalResult;
import com.wanted.clone.oneport.payments.application.result.PaymentCancelResult;
import com.wanted.clone.oneport.payments.application.result.PaymentSettlementResult;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.infrastructure.pg.PgPaymentGatewayException;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.request.TossApproveMessage;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.request.TossCancelMessage;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.TossApproveResponseMessage;
import com.wanted.clone.oneport.payments.infrastructure.pg.toss.response.TossCancelResponseMessage;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TossPayment implements PaymentAPIs {
    private final TossPaymentAPIs tossClient;
    private final ObjectMapper objectMapper;

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

        throw toPaymentGatewayException(response);
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

        throw toPaymentGatewayException(response);
    }

    @Override
    public List<PaymentSettlementResult> requestPaymentSettlement() throws IOException {
        return null;
    }

    private PgPaymentGatewayException toPaymentGatewayException(Response<?> response) throws IOException {
        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return new PgPaymentGatewayException(response.code(), null, response.message());
        }

        String errorJson = errorBody.string();
        try {
            TossErrorResponse errorResponse = objectMapper.readValue(errorJson, TossErrorResponse.class);
            String message = errorResponse.getMessage() == null || errorResponse.getMessage().isBlank()
                    ? response.message()
                    : errorResponse.getMessage();
            return new PgPaymentGatewayException(response.code(), errorResponse.getCode(), message);
        } catch (IOException ex) {
            return new PgPaymentGatewayException(response.code(), null, response.message());
        }
    }
}
