package com.wanted.clone.oneport.payments.domain.entity.order;

import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class OrderTests {

    @Test
    void orderPaymentFullFill_OrderCompleted_ChangesOrderAndItems() throws Exception {
        Order order = orderWithItems();

        order.orderPaymentFullFill("payment-key-1");

        Assertions.assertEquals(OrderStatus.PAYMENT_FULLFILL, order.getStatus());
        Assertions.assertEquals("payment-key-1", order.getPaymentId());
        Assertions.assertTrue(order.getItems().stream()
            .allMatch(item -> item.getState().equals(OrderStatus.PAYMENT_FULLFILL)));
    }

    @Test
    void orderPaymentFullFill_NotOrderCompleted_ThrowsException() throws Exception {
        Order order = orderWithItems();
        order.orderPaymentFullFill("payment-key-1");

        Assertions.assertThrows(
            PaymentRuleViolationException.class,
            () -> order.orderPaymentFullFill("payment-key-2")
        );
    }

    @Test
    void orderAllCancel_PaymentFulfilled_ChangesOrderAndItemsToCancelled() throws Exception {
        Order order = orderWithItems();
        order.orderPaymentFullFill("payment-key-1");

        order.orderAllCancel();

        Assertions.assertEquals(OrderStatus.ORDER_CANCELLED, order.getStatus());
        Assertions.assertTrue(order.getItems().stream()
            .allMatch(item -> item.getState().equals(OrderStatus.ORDER_CANCELLED)));
    }

    @Test
    void orderCancel_SelectedItem_ChangesOrderToPartialCancelled() throws Exception {
        Order order = orderWithItems();
        order.orderPaymentFullFill("payment-key-1");

        order.orderCancel(new int[]{1});

        Assertions.assertEquals(OrderStatus.ORDER_PARTIAL_CANCELLED, order.getStatus());
        Assertions.assertEquals(OrderStatus.ORDER_CANCELLED, order.getItems().get(0).getState());
        Assertions.assertEquals(OrderStatus.PAYMENT_FULLFILL, order.getItems().get(1).getState());
    }

    @Test
    void orderCancel_AllItems_ChangesOrderToCancelled() throws Exception {
        Order order = orderWithItems();
        order.orderPaymentFullFill("payment-key-1");

        order.orderCancel(new int[]{1, 2});

        Assertions.assertEquals(OrderStatus.ORDER_CANCELLED, order.getStatus());
    }

    @Test
    void orderCancel_RemainingItemAfterPartialCancel_ChangesOrderToCancelled() throws Exception {
        Order order = orderWithItems();
        order.orderPaymentFullFill("payment-key-1");
        order.orderCancel(new int[]{1});

        order.orderCancel(new int[]{2});

        Assertions.assertEquals(OrderStatus.ORDER_CANCELLED, order.getStatus());
        Assertions.assertTrue(order.getItems().stream()
            .allMatch(item -> item.getState().equals(OrderStatus.ORDER_CANCELLED)));
    }

    @Test
    void orderCancel_PurchaseDecisionOrder_ThrowsException() throws Exception {
        Order order = orderWithItems();
        order.setStatus(OrderStatus.PURCHASE_DECISION);

        Assertions.assertThrows(
            PaymentRuleViolationException.class,
            () -> order.orderCancel(new int[]{1})
        );
    }

    @Test
    void orderCancel_UnknownItem_ThrowsException() throws Exception {
        Order order = orderWithItems();
        order.orderPaymentFullFill("payment-key-1");

        Assertions.assertThrows(
            PaymentRuleViolationException.class,
            () -> order.orderCancel(new int[]{99})
        );
    }

    private Order orderWithItems() throws Exception {
        return new Order("order-1", "buyer", "010", List.of(
            orderItem(1),
            orderItem(2)
        ));
    }

    private OrderItem orderItem(int itemIdx) {
        return OrderItem.builder()
            .id(new PurchaseOrderId("order-1", itemIdx))
            .productId(UUID.randomUUID())
            .productName("product-" + itemIdx)
            .price(1000)
            .quantity(1)
            .amount(1000)
            .state(OrderStatus.ORDER_COMPLETED)
            .build();
    }
}
