package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.dto.response.AdminOrderListItemResponse;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.Product;
import com.groupproject.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findWithItemsByOrderId(Long orderId);

    @Query("""
    select new com.groupproject.ecommerce.dto.response.AdminOrderListItemResponse(
        o.orderId,
        o.orderCode,
        o.status,
        o.total,
        o.placedAt,
        u.userId,
        u.email
    )
    from Order o join o.user u
    where (:status is null or o.status = :status)
    order by o.placedAt desc
""")
    Page<AdminOrderListItemResponse> adminList(
            @Param("status") OrderStatus status,
            Pageable pageable
    );




}
