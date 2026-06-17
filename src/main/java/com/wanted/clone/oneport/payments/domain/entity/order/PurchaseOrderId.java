package com.wanted.clone.oneport.payments.domain.entity.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PurchaseOrderId implements Serializable {
    private String orderId;

    private int itemIdx;
}
