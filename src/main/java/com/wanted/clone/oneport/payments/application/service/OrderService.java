package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.command.CreateOrderCommand;
import com.wanted.clone.oneport.payments.application.port.in.CreateNewOrderUseCase;
import com.wanted.clone.oneport.payments.application.port.in.GetOrderInfoUseCase;
import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements CreateNewOrderUseCase, GetOrderInfoUseCase {
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public Order createOrder(CreateOrderCommand command) throws Exception {
        return orderRepository.save(command.toEntity());
    }

    @Override
    public Order getOrderInfo(String orderId) {
        return orderRepository.findById(orderId);
    }
}
