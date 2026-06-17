package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PurchaseOrderJpaId implements Serializable {
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "item_idx")
    private int itemIdx;
}
