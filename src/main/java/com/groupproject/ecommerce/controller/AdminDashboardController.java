package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.*;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller for Admin Dashboard / Statistics
 */
@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    private final DashboardService dashboardService;

    /**
     * Hiển thị trang dashboard
     */
    @GetMapping
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Thống kê");
        model.addAttribute("activeMenu", "dashboard");

        return "admin/dashboard";
    }

    /**
     * API: Lấy thống kê tổng quan
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<DashboardStatsDto> getStats(HttpSession session) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * API: Lấy doanh thu theo ngày
     */
    @GetMapping("/api/revenue")
    @ResponseBody
    public ResponseEntity<List<RevenueByDateDto>> getRevenue(
            @RequestParam(defaultValue = "30") int days,
            HttpSession session) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        List<RevenueByDateDto> data = dashboardService.getRevenueByDays(days);
        return ResponseEntity.ok(data);
    }

    /**
     * API: Lấy top sản phẩm bán chạy
     */
    @GetMapping("/api/top-products")
    @ResponseBody
    public ResponseEntity<List<TopProductDto>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        List<TopProductDto> data = dashboardService.getTopProducts(limit);
        return ResponseEntity.ok(data);
    }

    /**
     * API: Lấy top danh mục bán chạy
     */
    @GetMapping("/api/top-categories")
    @ResponseBody
    public ResponseEntity<List<TopCategoryDto>> getTopCategories(
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        List<TopCategoryDto> data = dashboardService.getTopCategories(limit);
        return ResponseEntity.ok(data);
    }
}
