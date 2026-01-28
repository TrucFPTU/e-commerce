package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.CartItem;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    
    Order createOrderFromCart(User user, List<CartItem> cartItems, String phone, String address, BigDecimal total);
    
    Order getOrderById(Long orderId);
    
    List<Order> getOrdersByUser(User user);
}
