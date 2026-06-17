package com.wanted.clone.oneport.payments.domain.entity.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Setter
@Getter
public class Order {
    private String orderId;

    private String name;

    private String phoneNumber;

    private String paymentId;

    private int totalPrice;

    private OrderStatus status;

    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    @Builder
    public Order(String orderId, String name, String phoneNumber, List<OrderItem> items) throws Exception {
        this.orderId = orderId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = OrderStatus.ORDER_COMPLETED;
        this.items = items;
    }

    public void orderPaymentFullFill(String paymentKey) {
        update(OrderStatus.PAYMENT_FULLFILL);
        this.paymentId = paymentKey;
    }

    public void orderAllCancel() {
        items.forEach(item -> item.update(OrderStatus.ORDER_CANCELLED));
        update(OrderStatus.ORDER_CANCELLED);
    }

    public void orderCancel(int[] itemIdxs) {
        for(int itemIdx : itemIdxs){
            orderCancelBy(itemIdx);
        }
    }

    private void orderCancelBy(int itemIdx) {
//        this.items.stream().filter(orderItem -> orderItem.getItemIdx() == itemIdx)
//            .forEach(item -> item.update(OrderStatus.ORDER_CANCELLED));
    }

    public static boolean verifyHaveAtLeastOneItem(List<OrderItem> items) {
        return items == null || items.isEmpty();
    }

    public boolean verifyDuplicateOrderItemId() {
        List<UUID> productIds = this.getItems().stream().map(OrderItem::getProductId).distinct().toList();
        if (!productIds.isEmpty()) return true;
        else throw new IllegalArgumentException();
    }

    public boolean isNotOrderStatusPurchaseDecision() {
        return !(this.status.equals(OrderStatus.PURCHASE_DECISION));
    }


    private Order update(OrderStatus status) {
        this.status = status;
        this.getItems().forEach(item -> item.update(status));
        return this;
    }

    public void calculateTotalAmount() {
        this.totalPrice = this.items.stream().map(OrderItem::calculateAmount).reduce(0, Integer::sum);
    }

}
