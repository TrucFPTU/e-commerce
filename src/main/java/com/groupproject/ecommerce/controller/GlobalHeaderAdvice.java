package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.service.inter.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalHeaderAdvice {

    private final CartService cartService;
    private static final String SESSION_USER = "LOGIN_USER";

    @ModelAttribute("user")
    public User user(HttpSession session) {
        return (User) session.getAttribute(SESSION_USER);
    }

    @ModelAttribute("cartCount")
    public Integer cartCount(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) return 0;
        return cartService.getCartCount(user);
    }
}
