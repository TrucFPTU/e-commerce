package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.CartItem;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.repository.OrderRepository;
import com.groupproject.ecommerce.service.inter.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public Order createOrderFromCart(User user, List<CartItem> cartItems, String phone, String address, BigDecimal total) {
        // Tạo order code unique
        String orderCode = generateOrderCode();

        // Tạo order mới
        Order order = new Order();
        order.setOrderCode(orderCode);
        order.setUser(user);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPhone(phone);
        order.setAddress(address);
        order.setTotal(total);
        order.setPlacedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserUserIdOrderByPlacedAtDesc(user.getUserId());
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
