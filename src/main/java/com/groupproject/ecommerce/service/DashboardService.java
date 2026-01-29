package com.groupproject.ecommerce.service;

import com.groupproject.ecommerce.dto.*;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.OrderItem;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.repository.OrderItemRepository;
import com.groupproject.ecommerce.repository.OrderRepository;
import com.groupproject.ecommerce.repository.ProductRepository;
import com.groupproject.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard statistics
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Lấy thống kê tổng quan
     */
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        
        stats.setTotalOrders(orderRepository.count());
        stats.setTotalRevenue(orderRepository.getTotalRevenueByStatus(OrderStatus.COMPLETED));
        stats.setTotalCustomers(userRepository.countByRole(Role.CUSTOMER));
        stats.setTotalProducts(productRepository.count());
        
        stats.setAwaitingPaymentOrders(orderRepository.countByStatus(OrderStatus.AWAITING_PAYMENT));
        stats.setProcessingOrders(orderRepository.countByStatus(OrderStatus.PROCESSING));
        stats.setShippingOrders(orderRepository.countByStatus(OrderStatus.SHIPPING));
        stats.setCompletedOrders(orderRepository.countByStatus(OrderStatus.COMPLETED));
        stats.setCancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED));
        
        return stats;
    }

    /**
     * Lấy doanh thu theo ngày trong N ngày gần nhất
     */
    public List<RevenueByDateDto> getRevenueByDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0);
        List<Order> orders = orderRepository.findOrdersAfterDate(startDate);
        
        // Chỉ lấy đơn COMPLETED
        List<Order> completedOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        // Group by date
        Map<LocalDate, List<Order>> ordersByDate = completedOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getPlacedAt().toLocalDate()));
        
        // Tạo danh sách kết quả với đầy đủ các ngày
        List<RevenueByDateDto> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            
            List<Order> dayOrders = ordersByDate.getOrDefault(date, Collections.emptyList());
            BigDecimal revenue = dayOrders.stream()
                    .map(Order::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            result.add(new RevenueByDateDto(dateStr, revenue, (long) dayOrders.size()));
        }
        
        return result;
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    public List<TopProductDto> getTopProducts(int limit) {
        List<OrderItem> items = orderItemRepository.findAllCompletedOrderItems();
        
        // Group by product
        Map<Long, List<OrderItem>> itemsByProduct = items.stream()
                .collect(Collectors.groupingBy(oi -> oi.getProduct().getProductId()));
        
        List<TopProductDto> products = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : itemsByProduct.entrySet()) {
            List<OrderItem> productItems = entry.getValue();
            OrderItem first = productItems.get(0);
            
            long totalSold = productItems.stream().mapToLong(OrderItem::getQuantity).sum();
            BigDecimal totalRevenue = productItems.stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            products.add(new TopProductDto(
                    first.getProduct().getProductId(),
                    first.getProduct().getName(),
                    totalSold,
                    totalRevenue
            ));
        }
        
        // Sort by quantity sold desc
        products.sort((a, b) -> Long.compare(b.getTotalSold(), a.getTotalSold()));
        
        return products.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Lấy top danh mục bán chạy
     */
    public List<TopCategoryDto> getTopCategories(int limit) {
        List<OrderItem> items = orderItemRepository.findAllCompletedOrderItems();
        
        // Group by category
        Map<Long, List<OrderItem>> itemsByCategory = items.stream()
                .collect(Collectors.groupingBy(oi -> oi.getProduct().getCategory().getCategoryId()));
        
        List<TopCategoryDto> categories = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : itemsByCategory.entrySet()) {
            List<OrderItem> categoryItems = entry.getValue();
            OrderItem first = categoryItems.get(0);
            
            long totalSold = categoryItems.stream().mapToLong(OrderItem::getQuantity).sum();
            BigDecimal totalRevenue = categoryItems.stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            categories.add(new TopCategoryDto(
                    first.getProduct().getCategory().getCategoryId(),
                    first.getProduct().getCategory().getName(),
                    totalSold,
                    totalRevenue
            ));
        }
        
        // Sort by quantity sold desc
        categories.sort((a, b) -> Long.compare(b.getTotalSold(), a.getTotalSold()));
        
        return categories.stream().limit(limit).collect(Collectors.toList());
    }
}
