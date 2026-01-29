package com.groupproject.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for top categories by revenue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopCategoryDto {
    private Long categoryId;
    private String categoryName;
    private Long totalSold;
    private BigDecimal totalRevenue;
}
