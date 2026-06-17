package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter;

import com.wanted.clone.oneport.payments.domain.entity.payment.card.AcquireStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AcquireStatusConverter implements AttributeConverter<AcquireStatus, String> {
    @Override
    public String convertToDatabaseColumn(AcquireStatus acquireStatus) {
        return acquireStatus.name();
    }

    @Override
    public AcquireStatus convertToEntityAttribute(String status) {
        return AcquireStatus.valueOf(status);
    }
}
