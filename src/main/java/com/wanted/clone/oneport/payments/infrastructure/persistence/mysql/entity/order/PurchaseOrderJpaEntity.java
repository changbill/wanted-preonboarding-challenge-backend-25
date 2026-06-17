package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order;

import com.wanted.clone.oneport.payments.domain.entity.order.OrderStatus;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter.OrderStatusConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_order")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PurchaseOrderJpaEntity {
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
    private List<OrderItemJpaEntity> items = new ArrayList<>();
}
