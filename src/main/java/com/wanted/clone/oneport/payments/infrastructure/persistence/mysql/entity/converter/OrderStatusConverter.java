package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter;

import com.wanted.clone.oneport.payments.domain.entity.order.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(OrderStatus orderStatus) {
        return orderStatus.name();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String status) {
        return OrderStatus.valueOf(status);
    }
}
