package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.mapper;

import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.domain.entity.order.OrderItem;
import com.wanted.clone.oneport.payments.domain.entity.order.PurchaseOrderId;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.OrderItemJpaEntity;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.PurchaseOrderJpaEntity;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.PurchaseOrderJpaId;

import java.util.ArrayList;
import java.util.List;

public class OrderPersistenceMapper {
    private OrderPersistenceMapper() {
    }

    public static PurchaseOrderJpaEntity toJpaEntity(Order order) {
        PurchaseOrderJpaEntity orderEntity = new PurchaseOrderJpaEntity(
                order.getOrderId(),
                order.getName(),
                order.getPhoneNumber(),
                order.getPaymentId(),
                order.getTotalPrice(),
                order.getStatus(),
                new ArrayList<>()
        );

        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
                .map(item -> toJpaEntity(item, orderEntity))
                .toList();
        orderEntity.getItems().addAll(itemEntities);
        return orderEntity;
    }

    private static OrderItemJpaEntity toJpaEntity(OrderItem item, PurchaseOrderJpaEntity orderEntity) {
        PurchaseOrderJpaId id = new PurchaseOrderJpaId(
                item.getId().getOrderId(),
                item.getId().getItemIdx()
        );

        return new OrderItemJpaEntity(
                id,
                orderEntity,
                item.getProductId(),
                item.getProductName(),
                item.getPrice(),
                item.getSize(),
                item.getAmount(),
                item.getQuantity(),
                item.getState()
        );
    }

    public static Order toDomain(PurchaseOrderJpaEntity entity) {
        Order order = new Order(
                entity.getOrderId(),
                entity.getName(),
                entity.getPhoneNumber(),
                entity.getPaymentId(),
                entity.getTotalPrice(),
                entity.getStatus(),
                new ArrayList<>()
        );

        List<OrderItem> items = entity.getItems().stream()
                .map(item -> toDomain(item, order))
                .toList();
        order.setItems(items);
        return order;
    }

    private static OrderItem toDomain(OrderItemJpaEntity entity, Order order) {
        PurchaseOrderId id = new PurchaseOrderId(
                entity.getId().getOrderId(),
                entity.getId().getItemIdx()
        );

        return new OrderItem(
                id,
                order,
                entity.getProductId(),
                entity.getProductName(),
                entity.getPrice(),
                entity.getSize(),
                entity.getAmount(),
                entity.getQuantity(),
                entity.getState()
        );
    }
}
