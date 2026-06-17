package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.order;

import com.wanted.clone.oneport.payments.application.port.out.repository.OrderRepository;
import com.wanted.clone.oneport.payments.domain.entity.order.Order;
import com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.mapper.OrderPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order findById(String id) {
        return jpaOrderRepository
                .findById(id)
                .map(OrderPersistenceMapper::toDomain)
                .orElseThrow(() -> new NoSuchElementException("OrderId not found"));
    }

    @Override
    public Order save(Order newOrder) {
        return OrderPersistenceMapper.toDomain(
                jpaOrderRepository.save(OrderPersistenceMapper.toJpaEntity(newOrder))
        );
    }

}
