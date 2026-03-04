package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.inter.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Admin Staff Management
 * Provides CRUD operations for managing staff and customer users
 */
@Controller
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final UserService userService;

    // Constants
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN_STAFF = "redirect:/admin/staff";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * Display staff management page
     */
    @GetMapping
    public String showStaffManagement(HttpSession session, Model model) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        List<User> staffUsers = userService.getUsersByRole(Role.STAFF);
        
        model.addAttribute("users", staffUsers);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Quản lý Nhân viên");
        model.addAttribute("activeMenu", "staff");

        return "admin/staff";
    }

    /**
     * Get user by ID (for editing)
     */
    @GetMapping("/{id}")
    @ResponseBody
    public User getUserById(@PathVariable Long id, HttpSession session) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return null;
        }

        return userService.getUserById(id);
    }

    /**
     * Save user (create or update)
     */
    @PostMapping("/save")
    public String saveUser(
            @RequestParam(required = false) Long userId,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam String fullName,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            // Check email uniqueness (except for current user)
            if (userService.emailExists(email, userId)) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
                return REDIRECT_ADMIN_STAFF;
            }

            userService.saveUser(userId, email, password, fullName, Role.STAFF);

            String message = userId == null ? "Thêm nhân viên thành công!" : "Cập nhật nhân viên thành công!";
            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_STAFF;
    }

    /**
     * Delete user
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            // Prevent admin from deleting themselves
            if (admin.getUserId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản của chính bạn!");
                return REDIRECT_ADMIN_STAFF;
            }

            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Xóa nhân viên thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_STAFF;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get authenticated user from session
     */
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }
}
