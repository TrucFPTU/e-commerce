package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Admin Customer Management
 * Provides CRUD operations for managing customer users
 */
@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constants
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN_CUSTOMERS = "redirect:/admin/customers";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * Display customer management page
     */
    @GetMapping
    public String showCustomerManagement(HttpSession session, Model model) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        List<User> customerUsers = userRepository.findByRole(Role.CUSTOMER);
        
        model.addAttribute("users", customerUsers);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Quản lý Khách hàng");
        model.addAttribute("activeMenu", "customers");

        return "admin/customers";
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

        return userRepository.findById(id).orElse(null);
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
            User user = findOrCreateUser(userId);

            // Check email uniqueness (except for current user)
            if (userId == null || !email.equals(user.getEmail())) {
                if (userRepository.findByEmail(email).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
                    return REDIRECT_ADMIN_CUSTOMERS;
                }
            }

            updateUserFields(user, email, password, fullName, Role.CUSTOMER);
            userRepository.save(user);

            String message = userId == null ? "Thêm khách hàng thành công!" : "Cập nhật khách hàng thành công!";
            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_CUSTOMERS;
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
                return REDIRECT_ADMIN_CUSTOMERS;
            }

            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khách hàng thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_CUSTOMERS;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get authenticated user from session
     */
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }

    /**
     * Find existing user or create new one
     */
    private User findOrCreateUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        }
        return new User();
    }

    /**
     * Update user fields
     */
    private void updateUserFields(User user, String email, String password, String fullName, Role role) {
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);

        // Only update password if provided (for edit mode)
        if (password != null && !password.isBlank()) {
            user.setPassWord(passwordEncoder.encode(password));
        } else if (user.getUserId() == null) {
            // For new user, password is required
            throw new RuntimeException("Mật khẩu không được để trống!");
        }
    }
}
