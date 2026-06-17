package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.mapper;

import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.domain.entity.order.OrderItem;
import com.wanted.clone.oneport.payments.domain.entity.order.OrderStatus;
import com.wanted.clone.oneport.payments.domain.entity.order.PurchaseOrderId;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.PurchaseOrderJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderPersistenceMapperTests {
    @Test
    void maps_order_between_domain_and_jpa_entity() {
        Order order = new Order(
                "order-1",
                "buyer",
                "010-0000-0000",
                "payment-1",
                1000,
                OrderStatus.PAYMENT_FULLFILL,
                List.of()
        );
        OrderItem item = new OrderItem(
                new PurchaseOrderId("order-1", 1),
                order,
                UUID.randomUUID(),
                "product",
                1000,
                "1ea",
                1000,
                1,
                OrderStatus.PAYMENT_FULLFILL
        );
        order.setItems(List.of(item));

        PurchaseOrderJpaEntity jpaEntity = OrderPersistenceMapper.toJpaEntity(order);
        Order mapped = OrderPersistenceMapper.toDomain(jpaEntity);

        assertThat(mapped.getOrderId()).isEqualTo(order.getOrderId());
        assertThat(mapped.getStatus()).isEqualTo(order.getStatus());
        assertThat(mapped.getItems()).hasSize(1);
        assertThat(mapped.getItems().get(0).getOrder()).isSameAs(mapped);
        assertThat(mapped.getItems().get(0).getProductId()).isEqualTo(item.getProductId());
    }
}
