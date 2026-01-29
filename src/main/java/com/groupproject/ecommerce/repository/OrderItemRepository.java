package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_OrderId(Long orderId);
    
    // Dashboard: Lấy tất cả order items từ đơn hoàn thành với fetch join
    @Query("SELECT oi FROM OrderItem oi " +
           "JOIN FETCH oi.product p " +
           "JOIN FETCH p.category " +
           "WHERE oi.order.status = com.groupproject.ecommerce.enums.OrderStatus.COMPLETED")
    List<OrderItem> findAllCompletedOrderItems();
}
