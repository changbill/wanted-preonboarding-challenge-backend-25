package com.wanted.clone.oneport.payments.application.service;

import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
import com.wanted.clone.oneport.payments.application.command.CreateOrderCommand;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.presentation.web.request.order.Orderer;
import com.wanted.clone.oneport.payments.presentation.web.request.order.ReqNewOrder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void createOrder_NewOrder_ANormalOrderForm() throws Exception {
        ReqNewOrder newOrder = new ReqNewOrder(new Orderer("유진호", "010-1234-1234"),
            List.of(new ReqNewOrder.OrderedItem(1, UUID.randomUUID(), "농심 짜파게티 4봉", 4500, 1, 4500)));
        CreateOrderCommand command = newOrder.toCommand();
        Order order = command.toEntity();

        Mockito.when(orderRepository.save(any())).thenReturn(order);
        Order completedOrder = orderService.createOrder(command);

        Mockito.verify(orderRepository, Mockito.times(1)).save(any());

        Assertions.assertEquals(order, completedOrder);
    }

}
