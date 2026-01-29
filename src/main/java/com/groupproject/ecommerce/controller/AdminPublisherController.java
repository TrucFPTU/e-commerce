package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.Publisher;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.inter.PublisherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Admin Publisher Management
 * Provides CRUD operations for managing publishers
 */
@Controller
@RequestMapping("/admin/publishers")
@RequiredArgsConstructor
public class AdminPublisherController {

    private final PublisherService publisherService;

    // Constants
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN_PUBLISHERS = "redirect:/admin/publishers";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * Display publisher management page
     */
    @GetMapping
    public String showPublisherManagement(HttpSession session, Model model) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        List<Publisher> publishers = publisherService.getAllPublishers();
        
        model.addAttribute("publishers", publishers);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Quản lý Nhà xuất bản");
        model.addAttribute("activeMenu", "publishers");

        return "admin/publishers";
    }

    /**
     * Get publisher by ID (for editing)
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Publisher getPublisherById(@PathVariable Long id, HttpSession session) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return null;
        }

        return publisherService.getById(id);
    }

    /**
     * Save publisher (create or update)
     */
    @PostMapping("/save")
    public String savePublisher(
            @RequestParam(required = false) Long publisherId,
            @RequestParam String name,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            publisherService.savePublisher(publisherId, name);

            String message = publisherId == null ? "Thêm nhà xuất bản thành công!" : "Cập nhật nhà xuất bản thành công!";
            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_PUBLISHERS;
    }

    /**
     * Delete publisher
     */
    @PostMapping("/delete/{id}")
    public String deletePublisher(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            publisherService.deletePublisher(id);
            redirectAttributes.addFlashAttribute("success", "Xóa nhà xuất bản thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_PUBLISHERS;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get authenticated user from session
     */
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }
}
