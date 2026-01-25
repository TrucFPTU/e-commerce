package com.groupproject.ecommerce.controller;


import com.groupproject.ecommerce.dto.request.ProfileUpdateReq;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.service.inter.ProductService;
import com.groupproject.ecommerce.service.inter.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class CustomerController {

    private User getUserOrRedirect(HttpSession session) {
        return (User) session.getAttribute("LOGIN_USER");
    }
    private final ProductService productService;
    private final UserService userService;

//    @GetMapping("/admin")
//    public String admin(Model model, HttpSession session) {
//        User user = getUserOrRedirect(session);
//        if (user == null) return "redirect:/login";
//        model.addAttribute("user", user);
//        return "dashboard/admin";
//    }

    @GetMapping("/homepage")
    public String customer(@RequestParam(value = "search", required = false) String search,
                           Model model,
                           HttpSession session) {
        User user = getUserOrRedirect(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);

        if (search == null || search.trim().isEmpty()) {
            model.addAttribute("books", productService.getHomeBooks());
        } else {
            String keyword = search.trim();
            model.addAttribute("search", keyword);
            model.addAttribute("books", productService.searchHomeBooks(keyword));
        }
        return "customer/home";
    }


    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "customer/profile"; // templates/customer/profile.html
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute ProfileUpdateReq req, HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";

        User updated = userService.updateProfile(user.getUserId(), req);

        // update lại session để header/profile hiển thị đúng
        session.setAttribute("LOGIN_USER", updated);

        return "redirect:/profile";
    }
}
