package com.wanted.clone.oneport.payments.domain.entity.order;

import lombok.*;

import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
public class OrderItem {
    private PurchaseOrderId id;

    private Order order;

    private UUID productId;

    private String productName;

    private int price;

    private String size;

    private int amount;

    private int quantity;

    private OrderStatus state;

    protected OrderItem() {
    }

    public void update(OrderStatus state) {
        this.state = state;
    }

    public int calculateAmount() {
        int totalPrice = price * quantity;
        this.amount = totalPrice;
        return totalPrice;
    }
}
