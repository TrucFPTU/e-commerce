package com.groupproject.ecommerce.controller;


import com.groupproject.ecommerce.entity.Product;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final ProductRepository productRepository;
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

        List<Product> products = productRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("products", products);
        return "dashboard/homepage";
    }
}
