package com.groupproject.ecommerce.dto.response;

import com.groupproject.ecommerce.enums.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminOrderListItemResponse {

    private Long orderId;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal total;
    private LocalDateTime placedAt;

    private Long userId;
    private String userEmail;
}
