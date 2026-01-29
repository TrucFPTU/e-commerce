package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Admin Dashboard / Statistics
 */
@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * Display dashboard page
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
}
