package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.order;


import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.PurchaseOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<PurchaseOrderJpaEntity, String> {
}
