package com.wanted.clone.oneport.payments.application.command;

import com.wanted.clone.oneport.core.common.IdGenerator;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.domain.entity.order.OrderItem;
import com.wanted.clone.oneport.payments.domain.entity.order.OrderStatus;
import com.wanted.clone.oneport.payments.domain.entity.order.PurchaseOrderId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateOrderCommand {
    private String ordererName;
    private String ordererPhoneNumber;
    private List<OrderedItemCommand> newlyOrderedItem;

    @Getter
    @AllArgsConstructor
    public static class OrderedItemCommand {
        private int itemIdx;
        private UUID productId;
        private String productName;
        private int price;
        private int quantity;
        private int amounts;
    }

    public Order toEntity() throws Exception {
        Order order = Order.builder()
            .orderId(IdGenerator.generateId(14))
            .items(new ArrayList<>())
            .name(this.getOrdererName())
            .phoneNumber(this.getOrdererPhoneNumber())
            .build();

        order.getItems().addAll(this.convertToOrderItems(order));
        if (Order.verifyHaveAtLeastOneItem(order.getItems())) {
            throw new Exception("Noting Items");
        }
        order.calculateTotalAmount();
        return order;
    }

    private List<OrderItem> convertToOrderItems(Order order) {
        return newlyOrderedItem.stream()
            .map(item -> convertToOrderItem(item, order))
            .toList();
    }

    private OrderItem convertToOrderItem(OrderedItemCommand item, Order order) {
        return OrderItem.builder()
            .order(order)
            .id(new PurchaseOrderId(order.getOrderId(), item.getItemIdx()))
            .productId(item.getProductId())
            .productName(item.getProductName())
            .price(item.getPrice())
            .quantity(item.getQuantity())
            .size("FREE")
            .state(OrderStatus.ORDER_COMPLETED)
            .build();
    }
}
