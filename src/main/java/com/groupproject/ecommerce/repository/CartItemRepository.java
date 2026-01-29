package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.CartItem;
import com.groupproject.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct_ProductId(User user, Long productId);
    void deleteByUser(User user);
    List<CartItem> findByUser_UserId(Long userId);
    @Query("select coalesce(sum(ci.quantity), 0) from CartItem ci where ci.user = :user")
    Integer sumQuantityByUser(@Param("user") User user);
}
