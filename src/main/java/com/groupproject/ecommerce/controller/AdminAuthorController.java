package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.Author;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.inter.AuthorService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Admin Author Management
 * Provides CRUD operations for managing authors
 */
@Controller
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

    private final AuthorService authorService;

    // Constants
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN_AUTHORS = "redirect:/admin/authors";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * Display author management page
     */
    @GetMapping
    public String showAuthorManagement(HttpSession session, Model model) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        List<Author> authors = authorService.getAllAuthors();
        
        model.addAttribute("authors", authors);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Quản lý Tác giả");
        model.addAttribute("activeMenu", "authors");

        return "admin/authors";
    }

    /**
     * Get author by ID (for editing)
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Author getAuthorById(@PathVariable Long id, HttpSession session) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return null;
        }

        return authorService.getById(id);
    }

    /**
     * Save author (create or update)
     */
    @PostMapping("/save")
    public String saveAuthor(
            @RequestParam(required = false) Long authorId,
            @RequestParam String name,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            authorService.saveAuthor(authorId, name);

            String message = authorId == null ? "Thêm tác giả thành công!" : "Cập nhật tác giả thành công!";
            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_AUTHORS;
    }

    /**
     * Delete author
     */
    @PostMapping("/delete/{id}")
    public String deleteAuthor(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User admin = getAuthenticatedUser(session);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return REDIRECT_LOGIN;
        }

        try {
            authorService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("success", "Xóa tác giả thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return REDIRECT_ADMIN_AUTHORS;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get authenticated user from session
     */
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }
}
