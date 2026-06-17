package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.order;


import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.OrderItemJpaEntity;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.PurchaseOrderJpaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderItemsRepository extends JpaRepository<OrderItemJpaEntity, PurchaseOrderJpaId> {
}
