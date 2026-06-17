package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter;

import com.wanted.clone.oneport.payments.domain.entity.payment.PaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, String> {
    @Override
    public String convertToDatabaseColumn(PaymentMethod paymentMethod) {
        return paymentMethod.getMethodName();
    }

    @Override
    public PaymentMethod convertToEntityAttribute(String methodName) {
        return PaymentMethod.fromMethodName(methodName);
    }
}
