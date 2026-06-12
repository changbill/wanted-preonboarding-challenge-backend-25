package com.wanted.clone.oneport.payments.application.port.in;

import com.wanted.clone.oneport.payments.domain.entity.order.Order;

public interface GetOrderInfoUseCase {
    Order getOrderInfo(String orderId);
}
