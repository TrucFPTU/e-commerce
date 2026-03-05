package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.*;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    private final DashboardService dashboardService;

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

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<DashboardStatsDto> getStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpSession session
    ) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        DashboardStatsDto stats = dashboardService.getDashboardStats(year, month);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/revenue")
    @ResponseBody
    public ResponseEntity<List<RevenueByDateDto>> getRevenue(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "30") int days, // fallback nếu không có year/month
            HttpSession session
    ) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        List<RevenueByDateDto> data = dashboardService.getRevenue(year, month, days);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/top-products")
    @ResponseBody
    public ResponseEntity<List<TopProductDto>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpSession session
    ) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        List<TopProductDto> data = dashboardService.getTopProducts(limit, year, month);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/top-categories")
    @ResponseBody
    public ResponseEntity<List<TopCategoryDto>> getTopCategories(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpSession session
    ) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        List<TopCategoryDto> data = dashboardService.getTopCategories(limit, year, month);
        return ResponseEntity.ok(data);
    }

    /**
     * Export full dashboard report PDF theo GLOBAL filter (year/month)
     */
    @GetMapping(value = "/export.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpSession session
    ) {
        User user = (User) session.getAttribute(LOGIN_USER_KEY);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(401).build();
        }

        byte[] pdfBytes = dashboardService.exportDashboardPdf(year, month);

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String filename = "dashboard_report_" + time + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}