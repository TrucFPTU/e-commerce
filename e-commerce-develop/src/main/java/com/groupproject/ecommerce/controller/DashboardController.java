package com.groupproject.ecommerce.controller;


import com.groupproject.ecommerce.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {

    private User getUserOrRedirect(HttpSession session) {
        return (User) session.getAttribute("LOGIN_USER");
    }

    @GetMapping("/admin")
    public String admin(Model model, HttpSession session) {
        User user = getUserOrRedirect(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "dashboard/admin";
    }


    @GetMapping("/homepage")
    public String customer(Model model, HttpSession session) {
        User user = getUserOrRedirect(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "dashboard/homepage";
    }
}
