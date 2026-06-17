package com.wanted.clone.oneport.payments.application.port.out.repository;

import com.wanted.clone.oneport.payments.domain.entity.order.Order;

public interface OrderRepository {
    Order findById(String id);
    Order findByIdForUpdate(String id);
    Order save(Order newOrder);
}
