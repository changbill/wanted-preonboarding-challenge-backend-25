package com.wanted.clone.oneport.payments.application.port.in;

import com.wanted.clone.oneport.payments.application.command.CreateOrderCommand;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;

public interface CreateNewOrderUseCase {
    Order createOrder(CreateOrderCommand command) throws Exception;
}
