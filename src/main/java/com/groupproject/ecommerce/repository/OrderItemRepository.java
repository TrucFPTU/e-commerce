package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.OrderItem;
import com.groupproject.ecommerce.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.product p " +
            "JOIN FETCH p.category " +
            "WHERE oi.order.status IN :statuses " +
            "AND oi.order.placedAt >= :start " +
            "AND oi.order.placedAt < :end")
    List<OrderItem> findOrderItemsByStatusesAndPlacedAtBetween(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
