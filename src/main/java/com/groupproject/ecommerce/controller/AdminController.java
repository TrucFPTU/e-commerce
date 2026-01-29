package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.*;
import com.groupproject.ecommerce.enums.ProductStatus;
import com.groupproject.ecommerce.service.inter.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final SupplierService supplierService;
    private final AuthorService authorService;

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN = "redirect:/admin";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    @GetMapping
    public String adminDashboard(Model model, HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        populateModelWithData(model, user);
        return "admin/products";
    }

    @GetMapping("/products/{id}")
    @ResponseBody
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/products/save")
    public String saveProduct(
            @RequestParam(required = false) Long productId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) Integer publishYear,
            @RequestParam Integer stock,
            @RequestParam ProductStatus status,
            @RequestParam Long categoryId,
            @RequestParam Long publisherId,
            @RequestParam Long supplierId,
            @RequestParam List<Long> authorIds,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (getAuthenticatedUser(session) == null) {
            return REDIRECT_LOGIN;
        }

        try {
            productService.saveProduct(productId, name, description, imageUrl, price, publishYear, 
                                      stock, status, categoryId, publisherId, supplierId, authorIds);

            redirectAttributes.addFlashAttribute("message", 
                productId != null ? "Cập nhật sách thành công!" : "Thêm sách mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return REDIRECT_ADMIN;
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (getAuthenticatedUser(session) == null) {
            return REDIRECT_LOGIN;
        }

        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Xóa sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sách: " + e.getMessage());
        }

        return REDIRECT_ADMIN;
    }

    // Private helper methods
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }

    private void populateModelWithData(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("publishers", publisherService.getAllPublishers());
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("authors", authorService.getAllAuthors());
    }
}
