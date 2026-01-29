package com.groupproject.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for revenue statistics by date
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByDateDto {
    private String date;           // Ngày (yyyy-MM-dd)
    private BigDecimal revenue;    // Doanh thu
    private Long orderCount;       // Số đơn hàng
}
