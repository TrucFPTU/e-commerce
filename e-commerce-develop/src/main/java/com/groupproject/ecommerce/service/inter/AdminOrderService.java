package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.response.AdminOrderDetailResponse;
import com.groupproject.ecommerce.dto.response.AdminOrderListItemResponse;
import com.groupproject.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;

public interface AdminOrderService {

    AdminOrderDetailResponse getOrderDetail(Long orderId);
    Page<AdminOrderListItemResponse> listOrders(OrderStatus status, int page, int size);

    void updateStatus(Long orderId, OrderStatus status);


}
