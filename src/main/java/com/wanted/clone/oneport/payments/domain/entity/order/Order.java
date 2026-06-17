package com.wanted.clone.oneport.payments.domain.entity.order;

import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "purchase_order")
@AllArgsConstructor
@Setter
@Getter
public class Order {
    @Id
    @Column(name = "order_id")
    private String orderId;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "total_price")
    private int totalPrice;

    @Column(name = "order_state")
    @Convert(converter = OrderStatusConverter.class)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
        if (!this.status.equals(OrderStatus.ORDER_COMPLETED)) {
            throw PaymentRuleViolationException.notPayableOrder(orderId);
        }
        update(OrderStatus.PAYMENT_FULLFILL);
        this.paymentId = paymentKey;
    }

    public void orderAllCancel() {
        verifyCancellable();
        items.forEach(item -> item.update(OrderStatus.ORDER_CANCELLED));
        updateOrderStatusOnly(OrderStatus.ORDER_CANCELLED);
    }

    public void orderCancel(int[] itemIdxs) {
        verifyCancellable();
        for(int itemIdx : itemIdxs){
            orderCancelBy(itemIdx);
        }
        if (isAllItemsCancelled()) {
            updateOrderStatusOnly(OrderStatus.ORDER_CANCELLED);
            return;
        }
        updateOrderStatusOnly(OrderStatus.ORDER_PARTIAL_CANCELLED);
    }

    private void orderCancelBy(int itemIdx) {
        this.items.stream()
            .filter(orderItem -> orderItem.getId().getItemIdx() == itemIdx)
            .findFirst()
            .orElseThrow(() -> PaymentRuleViolationException.orderItemNotFound(itemIdx))
            .update(OrderStatus.ORDER_CANCELLED);
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

    public boolean isPaymentFulfilled() {
        return this.status.equals(OrderStatus.PAYMENT_FULLFILL)
            || this.status.equals(OrderStatus.ORDER_PARTIAL_CANCELLED)
            || this.status.equals(OrderStatus.ORDER_CANCELLED);
    }

    public boolean isPaidWith(String paymentKey) {
        return isPaymentFulfilled() && Objects.equals(this.paymentId, paymentKey);
    }

    private Order update(OrderStatus status) {
        this.status = status;
        this.getItems().forEach(item -> item.update(status));
        return this;
    }

    private void updateOrderStatusOnly(OrderStatus status) {
        this.status = status;
    }

    private void verifyCancellable() {
        if (this.status.equals(OrderStatus.PURCHASE_DECISION)) {
            throw PaymentRuleViolationException.purchaseDecisionOrder(orderId);
        }
    }

    private boolean isAllItemsCancelled() {
        return this.items.stream()
            .allMatch(item -> item.getState().equals(OrderStatus.ORDER_CANCELLED));
    }

    public void calculateTotalAmount() {
        this.totalPrice = this.items.stream().map(OrderItem::calculateAmount).reduce(0, Integer::sum);
    }

}
