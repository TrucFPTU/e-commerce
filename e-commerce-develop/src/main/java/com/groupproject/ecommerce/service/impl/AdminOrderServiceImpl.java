package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.dto.response.AdminOrderDetailResponse;
import com.groupproject.ecommerce.dto.response.AdminOrderItemResponse;
import com.groupproject.ecommerce.dto.response.AdminOrderListItemResponse;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.repository.OrderRepository;
import com.groupproject.ecommerce.service.inter.AdminOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;

    @Override
    public Page<AdminOrderListItemResponse> listOrders(OrderStatus status, int page, int size) {
        return orderRepository.adminList(status, PageRequest.of(page, size));
    }

    @Override
    public AdminOrderDetailResponse getOrderDetail(Long orderId) {
        Order o = orderRepository.findWithItemsByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<AdminOrderItemResponse> items = o.getItems().stream()
                .map(i -> new AdminOrderItemResponse(
                        i.getProductNameSnapshot(),
                        i.getUnitPriceSnapshot(),
                        i.getQuantity(),
                        i.getLineTotal()
                ))
                .toList();

        return new AdminOrderDetailResponse(
                o.getOrderId(),
                o.getOrderCode(),
                o.getStatus(),
                o.getPhone(),
                o.getAddress(),
                o.getTotal(),
                o.getPlacedAt(),
                items
        );
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Nếu muốn chặt chẽ: chặn đổi status từ COMPLETED/CANCELLED
        // (bạn có thể bỏ nếu không cần)
        if (o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot change status of a finished order");
        }

        o.setStatus(status);
        // Không cần save() vì @Transactional + JPA dirty checking
    }

}
