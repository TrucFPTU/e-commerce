package com.groupproject.ecommerce.repository;

import java.util.List;

import com.groupproject.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByUserUserIdOrderByPlacedAtDesc(Long userId);
    Order findByOrderCode(String orderCode);

    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusOrderByPlacedAtDesc(OrderStatus status);
//
    
    List<Order> findByUserUserIdAndStatusOrderByPlacedAtDesc(Long userId, OrderStatus status);
}
