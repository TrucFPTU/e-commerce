package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.dto.request.CheckoutRequest;
import com.groupproject.ecommerce.entity.*;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.enums.PaymentStatus;
import com.groupproject.ecommerce.repository.CartItemRepository;
import com.groupproject.ecommerce.repository.OrderItemRepository;
import com.groupproject.ecommerce.repository.OrderRepository;
import com.groupproject.ecommerce.repository.ProductRepository;
import com.groupproject.ecommerce.service.inter.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final  OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void cancelOrder(User user, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // chặn hủy đơn người khác
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Không có quyền hủy đơn này");
        }

        // chỉ cho hủy khi chưa giao
        if (order.getStatus() == OrderStatus.SHIPPING ||
                order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy đơn đang giao hoặc đã hoàn thành");
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }


    @Override
    @Transactional
    public Order checkout(User user, CheckoutRequest request, List<Long> selectedCartItemIds) {

        List<CartItem> cartItems = cartItemRepository
                .findByUser_UserId(user.getUserId())
                .stream()
                .filter(ci -> selectedCartItemIds.contains(ci.getCartItemId()))
                .toList();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("No valid cart items selected");
        }

        Order order = new Order();
        order.setUser(user);
        order.setPhone(request.getPhone());
        order.setAddress(request.getAddress());
        order.setStatus(
                "CASH".equalsIgnoreCase(request.getPaymentMethod())
                        ? OrderStatus.PROCESSING
                        : OrderStatus.AWAITING_PAYMENT
        );
        order.setOrderCode(UUID.randomUUID().toString());
        order.setPlacedAt(LocalDateTime.now());
        order.setTotal(BigDecimal.ZERO);       // 👈 THÊM DÒNG NÀY
        order = orderRepository.save(order);


        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cart : cartItems) {

            Product product = cart.getProduct();

            if (product.getStock() < cart.getQuantity()) {
                throw new RuntimeException("Out of stock: " + product.getName());
            }

            // ✅ TRỪ STOCK
            product.setStock(product.getStock() - cart.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductNameSnapshot(product.getName());
            item.setUnitPriceSnapshot(product.getPrice());
            item.setQuantity(cart.getQuantity());

            BigDecimal lineTotal =
                    product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            item.setLineTotal(lineTotal);

            orderItemRepository.save(item);
            total = total.add(lineTotal);
        }

        order.setTotal(total);
        orderRepository.save(order);

        // ✅ XOÁ CHỈ CART ĐÃ CHECKOUT
        cartItemRepository.deleteAll(cartItems);

        return order;
    }




    @Override
    @Transactional
    public void updateOrderStatusAfterPayment(Order order, PaymentStatus paymentStatus) {

        switch (paymentStatus) {
            case SUCCESS -> order.setStatus(OrderStatus.PROCESSING);

            case PENDING, FAILED ->
                    order.setStatus(OrderStatus.AWAITING_PAYMENT);

            case REFUNDED ->
                    order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }


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

    @Override
    public List<Order> getOrdersByUserAndStatus(User user, OrderStatus status) {
        return orderRepository.findByUserUserIdAndStatusOrderByPlacedAtDesc(user.getUserId(), status);
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

}
