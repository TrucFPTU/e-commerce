package com.groupproject.ecommerce.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByUserUserIdOrderByPlacedAtDesc(Long userId);
    Order findByOrderCode(String orderCode);

    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusOrderByPlacedAtDesc(OrderStatus status);
    
    List<Order> findByUserUserIdAndStatusOrderByPlacedAtDesc(Long userId, OrderStatus status);
    
    // Dashboard: Tổng doanh thu từ đơn hoàn thành
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = :status")
    BigDecimal getTotalRevenueByStatus(@Param("status") OrderStatus status);
    
    // Dashboard: Đếm đơn hàng theo trạng thái
    Long countByStatus(OrderStatus status);
    
    // Dashboard: Lấy đơn hàng trong khoảng thời gian
    @Query("SELECT o FROM Order o WHERE o.placedAt >= :startDate ORDER BY o.placedAt DESC")
    List<Order> findOrdersAfterDate(@Param("startDate") LocalDateTime startDate);
}
