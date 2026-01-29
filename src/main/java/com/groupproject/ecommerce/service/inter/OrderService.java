package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.request.CheckoutRequest;
import com.groupproject.ecommerce.entity.CartItem;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.OrderItem;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    
    Order createOrderFromCart(User user, List<CartItem> cartItems, String phone, String address, BigDecimal total);
    
    Order getOrderById(Long orderId);
    
    List<Order> getOrdersByUser(User user);
    
    List<Order> getOrdersByUserAndStatus(User user, OrderStatus status);
    void updateOrderStatusAfterPayment(Order order, PaymentStatus paymentStatus);
    Order checkout(User user, CheckoutRequest request, List<Long> selectedCartItemIds);
    void cancelOrder(User user, Long orderId);
    List<Order> getOrdersWaitingConfirm();      // PROCESSING
    Order getOrderOrThrow(Long orderId);
    List<OrderItem> getOrderItems(Long orderId);
    List<Order> getOrdersByStatus(OrderStatus status);
    void confirm(Long orderId);
    void ship(Long orderId);
    void complete(Long orderId);
    void cancel(Long orderId);
    Order getOrderByCode(String orderCode);



}
