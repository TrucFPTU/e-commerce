package com.groupproject.ecommerce.controller;


import com.groupproject.ecommerce.dto.request.ProfileUpdateReq;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.service.inter.OrderService;
import com.groupproject.ecommerce.service.inter.ProductService;
import com.groupproject.ecommerce.service.inter.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class CustomerController {

    private User getUserOrRedirect(HttpSession session) {
        return (User) session.getAttribute("LOGIN_USER");
    }
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;


    @GetMapping("/homepage")
    public String customer(@RequestParam(value = "search", required = false) String search,
                           Model model,
                           HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
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
    @PostMapping("/orders/{orderId}/not-received")
    public String notReceived(@PathVariable Long orderId, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";

        try {
            orderService.reportNotReceived(orderId, user);
            ra.addFlashAttribute("errorMessage", "Vui lòng nhắn tin cho shop để nhận được hỗ trợ sớm nhất.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/orders?status=ISSUE";
    }
    @PostMapping("/orders/{orderId}/issue-received")
    public String issueReceived(@PathVariable Long orderId, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";

        try {
            orderService.issueReceived(orderId, user);
            ra.addFlashAttribute("successMessage", "Đã ghi nhận xác nhận của bạn. Shop sẽ đóng đơn sau khi kiểm tra.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/orders?status=ISSUE_RECEIVED";
    }
}
