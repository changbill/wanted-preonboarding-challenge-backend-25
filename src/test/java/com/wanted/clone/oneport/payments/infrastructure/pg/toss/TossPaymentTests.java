package com.wanted.clone.oneport.payments.infrastructure.pg.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.application.command.CancelPaymentCommand;
import com.wanted.clone.oneport.payments.application.result.PaymentApprovalResult;
import com.wanted.clone.oneport.payments.application.result.PaymentCancelResult;
import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.infrastructure.pg.PgPaymentGatewayException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

class TossPaymentTests {

    @Test
    void requestPaymentApprove_TossSuccessBody_ReturnsCommonApprovalResult() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                              "paymentKey":"payment-key",
                              "orderId":"order-id",
                              "method":"카드",
                              "status":"DONE",
                              "totalAmount":1000,
                              "balanceAmount":1000
                            }
                            """));

            TossPayment tossPayment = tossPayment(server);

            PaymentApprovalResult result = tossPayment.requestPaymentApprove(approveCommand());
            RecordedRequest request = server.takeRequest();

            Assertions.assertEquals("/payments/confirm", request.getPath());
            Assertions.assertEquals("payment-key", result.getTransactionId());
            Assertions.assertEquals(PaymentStatus.DONE, result.getStatus());
            Assertions.assertEquals(1000, result.getBalanceAmount());
        }
    }

    @Test
    void requestPaymentApprove_TossErrorBody_ThrowsCommonPgException() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {"code":"INVALID_REQUEST","message":"Invalid payment request."}
                            """));

            TossPayment tossPayment = tossPayment(server);

            PgPaymentGatewayException exception = Assertions.assertThrows(
                    PgPaymentGatewayException.class,
                    () -> tossPayment.requestPaymentApprove(approveCommand())
            );

            Assertions.assertEquals(400, exception.getStatusCode());
            Assertions.assertEquals("INVALID_REQUEST", exception.getErrorCode());
            Assertions.assertEquals("Invalid payment request.", exception.getMessage());
        }
    }

    @Test
    void requestPaymentCancel_TossSuccessBody_ReturnsCommonCancelResult() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                              "paymentKey":"payment-key",
                              "orderId":"order-id",
                              "method":"카드",
                              "status":"CANCELED",
                              "totalAmount":1000,
                              "balanceAmount":0,
                              "cancels":[{"cancelAmount":1000}]
                            }
                            """));

            TossPayment tossPayment = tossPayment(server);

            PaymentCancelResult result = tossPayment.requestPaymentCancel(
                    "payment-key",
                    new CancelPaymentCommand("order-id", null, "cancel", "payment-key", 1000)
            );
            RecordedRequest request = server.takeRequest();

            Assertions.assertEquals("/payments/payment-key/cancel", request.getPath());
            Assertions.assertEquals("payment-key", result.getTransactionId());
            Assertions.assertEquals(PaymentStatus.CANCELED, result.getStatus());
            Assertions.assertEquals(1000, result.getCanceledAmount());
        }
    }

    private TossPayment tossPayment(MockWebServer server) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        TossPaymentAPIs tossClient = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()
                .create(TossPaymentAPIs.class);
        return new TossPayment(tossClient, objectMapper);
    }

    private ApprovePaymentCommand approveCommand() {
        return ApprovePaymentCommand.builder()
                .selectedPgCorp(PgCorp.TOSS)
                .paymentKey("payment-key")
                .orderId("order-id")
                .totalAmount(1000)
                .build();
    }
}
