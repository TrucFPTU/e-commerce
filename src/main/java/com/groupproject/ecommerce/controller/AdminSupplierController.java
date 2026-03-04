package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.Supplier;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.inter.SupplierService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Admin Supplier Management
 * Provides CRUD operations for managing suppliers
 */
@Controller
@RequestMapping("/admin/suppliers")
@RequiredArgsConstructor
public class AdminSupplierController {

    private final SupplierService supplierService;

    // Constants
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN_SUPPLIERS = "redirect:/admin/suppliers";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * Display supplier management page
     */
    @GetMapping
    public String showSupplierManagement(HttpSession session, Model model) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        List<Supplier> suppliers = supplierService.getAllSuppliers();
        
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Quản lý Nhà cung cấp");
        model.addAttribute("activeMenu", "suppliers");

        return "admin/suppliers";
    }

    /**
     * Get supplier by ID (for editing)
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Supplier getSupplierById(@PathVariable Long id, HttpSession session) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return null;
        }

        return supplierService.getById(id);
    }

    /**
     * Save supplier (create or update)
     */
    @PostMapping("/save")
    public String saveSupplier(
            @RequestParam(required = false) Long supplierId,
            @RequestParam String name,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            supplierService.saveSupplier(supplierId, name);

            String message = supplierId == null ? "Thêm nhà cung cấp thành công!" : "Cập nhật nhà cung cấp thành công!";
            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_SUPPLIERS;
    }

    /**
     * Delete supplier
     */
    @PostMapping("/delete/{id}")
    public String deleteSupplier(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", "Xóa nhà cung cấp thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_SUPPLIERS;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get authenticated user from session
     */
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }
}
