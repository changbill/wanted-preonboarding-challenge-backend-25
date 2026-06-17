package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.order;


import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.order.PurchaseOrderJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<PurchaseOrderJpaEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrderJpaEntity o where o.orderId = :id")
    Optional<PurchaseOrderJpaEntity> findByIdForUpdate(@Param("id") String id);
}
