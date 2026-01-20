package com.groupproject.ecommerce.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
public class AdminOrderItemResponse {
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
