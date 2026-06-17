package com.wanted.clone.oneport.payments.infrastructure.pg.toss;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "pg.tosspayments")
public class TossPaymentProperties {
    @NotBlank
    private String baseUrl;

    @NotBlank
    private String secretKey;

    @Positive
    private long connectTimeoutSeconds = 20;

    @Positive
    private long writeTimeoutSeconds = 60;

    @Positive
    private long readTimeoutSeconds = 60;

    private boolean retryOnConnectionFailure = true;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(long connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public long getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    public void setWriteTimeoutSeconds(long writeTimeoutSeconds) {
        this.writeTimeoutSeconds = writeTimeoutSeconds;
    }

    public long getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(long readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public boolean isRetryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    public void setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
    }
}
