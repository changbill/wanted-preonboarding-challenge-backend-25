package com.wanted.clone.oneport.payments.infrastructure.pg.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wanted.clone.oneport.payments.application.command.ApprovePaymentCommand;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.infrastructure.pg.PgPaymentGatewayException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

class TossPaymentTests {

    @Test
    void requestPaymentApprove_TossErrorBody_ThrowsCommonPgException() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {"code":"INVALID_REQUEST","message":"Invalid payment request."}
                            """));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            TossPaymentAPIs tossClient = new Retrofit.Builder()
                    .baseUrl(server.url("/"))
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .build()
                    .create(TossPaymentAPIs.class);
            TossPayment tossPayment = new TossPayment(tossClient, objectMapper);

            ApprovePaymentCommand command = ApprovePaymentCommand.builder()
                    .selectedPgCorp(PgCorp.TOSS)
                    .paymentKey("payment-key")
                    .orderId("order-id")
                    .totalAmount(1000)
                    .build();

            PgPaymentGatewayException exception = Assertions.assertThrows(
                    PgPaymentGatewayException.class,
                    () -> tossPayment.requestPaymentApprove(command)
            );

            Assertions.assertEquals(400, exception.getStatusCode());
            Assertions.assertEquals("INVALID_REQUEST", exception.getErrorCode());
            Assertions.assertEquals("Invalid payment request.", exception.getMessage());
        }
    }
}
