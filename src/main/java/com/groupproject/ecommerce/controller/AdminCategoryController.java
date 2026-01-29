package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.Category;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.inter.CategoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Admin Category Management
 * Provides CRUD operations for managing categories
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    // Constants
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN_CATEGORIES = "redirect:/admin/categories";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * Display category management page
     */
    @GetMapping
    public String showCategoryManagement(HttpSession session, Model model) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        List<Category> categories = categoryService.getAllCategories();
        
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Quản lý Thể loại");
        model.addAttribute("activeMenu", "categories");

        return "admin/categories";
    }

    /**
     * Get category by ID (for editing)
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Category getCategoryById(@PathVariable Long id, HttpSession session) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return null;
        }

        return categoryService.getCategoryById(id);
    }

    /**
     * Save category (create or update)
     */
    @PostMapping("/save")
    public String saveCategory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam String name,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            categoryService.saveCategory(categoryId, name);

            String message = categoryId == null ? "Thêm thể loại thành công!" : "Cập nhật thể loại thành công!";
            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_CATEGORIES;
    }

    /**
     * Delete category
     */
    @PostMapping("/delete/{id}")
    public String deleteCategory(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thể loại thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_CATEGORIES;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get authenticated user from session
     */
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }
}
