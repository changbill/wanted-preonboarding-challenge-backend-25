package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {
    @Override
    public String convertToDatabaseColumn(PaymentStatus status) {
        return status.name();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String status) {
        return PaymentStatus.valueOf(status);
    }
}
