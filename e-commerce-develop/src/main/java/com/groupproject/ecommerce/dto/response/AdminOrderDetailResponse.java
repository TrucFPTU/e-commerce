package com.groupproject.ecommerce.dto.response;

import com.groupproject.ecommerce.enums.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class AdminOrderDetailResponse {
    private Long orderId;
    private String orderCode;
    private OrderStatus status;
    private String phone;
    private String address;
    private BigDecimal total;
    private LocalDateTime placedAt;
    private List<AdminOrderItemResponse> items;
}
