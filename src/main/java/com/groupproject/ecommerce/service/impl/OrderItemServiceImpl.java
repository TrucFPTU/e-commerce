package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.OrderItem;
import com.groupproject.ecommerce.repository.OrderItemRepository;
import com.groupproject.ecommerce.service.inter.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> getItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId);
    }
}
