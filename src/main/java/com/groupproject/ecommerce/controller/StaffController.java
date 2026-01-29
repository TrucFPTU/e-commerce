package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.response.BookCardRes;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.ConversationStatus;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.repository.ConversationRepo;
import com.groupproject.ecommerce.service.inter.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffController {

    private final ConversationRepo conversationRepo;
    private final ProductService productService;

    private User requireStaff(HttpSession session) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) return null;
        if (u.getRole() != Role.STAFF) return null;
        return u;
    }

    @GetMapping
    public String staffHome(HttpSession session) {
        User staff = requireStaff(session);
        if (staff == null) return "redirect:/login";
        return "redirect:/staff/chat";
    }

    @GetMapping("/chat")
    public String chat(Model model, HttpSession session) {
        User staff = requireStaff(session);
        if (staff == null) return "redirect:/login";

        var conversations = conversationRepo
                .findByStaff_UserIdAndStatusOrderByLastMessageAtDesc(staff.getUserId(), ConversationStatus.OPEN);

        model.addAttribute("user", staff);
        model.addAttribute("conversations", conversations);
        return "staff/chat"; // templates/staff/chat.html (UI bạn sẽ làm)
    }

    @GetMapping("/orders")
    public String orders(Model model, HttpSession session) {
        User staff = requireStaff(session);
        if (staff == null) return "redirect:/login";

        model.addAttribute("user", staff);
        return "staff/orders"; // placeholder
    }

    @GetMapping("/inventory")
    public String inventory(@RequestParam(name = "q", required = false) String q,
                            Model model,
                            HttpSession session) {
        User staff = requireStaff(session);
        if (staff == null) return "redirect:/login";

        String keyword = (q == null) ? "" : q.trim();

        List<BookCardRes> books = keyword.isBlank()
                ? productService.getHomeBooks()
                : productService.searchHomeBooks(keyword);

        model.addAttribute("user", staff);
        model.addAttribute("q", keyword);
        model.addAttribute("books", books);
        model.addAttribute("total", books.size());
        return "staff/inventory";
    }
    @GetMapping("/inventory/search")
    @ResponseBody
    public Map<String, Object> inventoryLiveSearch(
            @RequestParam(name = "q", required = false) String q,
            HttpSession session
    ) {
        User staff = requireStaff(session);
        if (staff == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String keyword = (q == null) ? "" : q.trim();

        List<BookCardRes> books = keyword.isBlank()
                ? productService.getHomeBooks()
                : productService.searchHomeBooks(keyword);

        return Map.of(
                "total", books.size(),
                "books", books
        );
    }
}
