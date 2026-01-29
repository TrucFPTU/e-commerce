package com.groupproject.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO chứa thống kê tổng quan cho Dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long totalCustomers;
    private Long totalProducts;
    
    // Thống kê theo trạng thái đơn hàng
    private Long awaitingPaymentOrders;
    private Long processingOrders;
    private Long shippingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
}
