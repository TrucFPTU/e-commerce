package com.groupproject.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByUserUserIdOrderByPlacedAtDesc(Long userId);
    
    List<Order> findByStatus(OrderStatus status);
}
