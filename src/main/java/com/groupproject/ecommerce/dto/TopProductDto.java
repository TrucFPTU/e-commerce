package com.groupproject.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for top selling products statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    private Long productId;
    private String productName;
    private Long totalSold;         // Tổng số lượng bán
    private BigDecimal totalRevenue; // Tổng doanh thu
}
