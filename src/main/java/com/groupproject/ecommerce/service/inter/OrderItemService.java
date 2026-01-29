package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.OrderItem;

import java.util.List;

public interface OrderItemService {
    List<OrderItem> getItemsByOrderId(Long orderId);
}