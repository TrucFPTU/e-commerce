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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Order getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return order;
    }


    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByPlacedAtDesc(status);
    }


    @Override
    public List<Order> getOrdersWaitingConfirm() {
        return orderRepository.findByStatusOrderByPlacedAtDesc(OrderStatus.PROCESSING);
    }

    @Override
    public Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        // optional: check order exists
        getOrderOrThrow(orderId);
        return orderItemRepository.findByOrder_OrderId(orderId);
    }

    @Override
    @Transactional
    public void confirm(Long orderId) {
        transition(orderId, OrderStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public void ship(Long orderId) {
        transition(orderId, OrderStatus.SHIPPING);
    }

    @Override
    @Transactional
    public void complete(Long orderId) {
        transition(orderId, OrderStatus.COMPLETED);
    }

    @Override
    @Transactional
    public void cancel(Long orderId) {
        transition(orderId, OrderStatus.CANCELLED);
    }

    private void transition(Long orderId, OrderStatus to) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus from = order.getStatus();

        if (!isAllowed(from, to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid transition: " + from + " -> " + to
            );
        }

        order.setStatus(to);
        // dirty checking tự update
    }

    private boolean isAllowed(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) return false;
        if (from == to) return true;

        return switch (from) {
            case PROCESSING -> (to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED);
            case CONFIRMED  -> (to == OrderStatus.SHIPPING  || to == OrderStatus.CANCELLED);
            case SHIPPING   -> (to == OrderStatus.COMPLETED);
            default -> false; // COMPLETED/CANCELLED/RETURNED/AWAITING_PAYMENT không cho staff đổi bậy
        };
    }

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
