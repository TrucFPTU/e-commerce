package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.Publisher;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.service.inter.AuthorService;
import com.groupproject.ecommerce.service.inter.ProductService;
import com.groupproject.ecommerce.service.inter.PublisherService;
import com.groupproject.ecommerce.service.inter.SupplierService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuthorService authorService;
    private final PublisherService publisherService;
    private final SupplierService supplierService;

    private User getUserOrRedirect(HttpSession session) {
        return (User) session.getAttribute("LOGIN_USER");
    }

    @GetMapping("/book/{id}")
    public String bookDetail(@PathVariable Long id, Model model, HttpSession session) {
        User user = getUserOrRedirect(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("book", productService.getBookCardById(id));
        model.addAttribute("relatedBooks", productService.getRelatedBooks(id));

        return "customer/book";
    }

    @GetMapping("/author/{id}")
    public String authorDetail(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("author", authorService.getById(id));
        model.addAttribute("books", productService.getBooksByAuthor(id));

        return "customer/author";
    }

    @GetMapping("/publisher/{id}")
    public String publisherDetail(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("publisher", publisherService.getById(id)); // entity Publisher
        model.addAttribute("books", productService.getBooksByPublisher(id)); // List<BookCardRes>

        return "customer/publisher";
    }

    @GetMapping("/supplier/{id}")
    public String supplierDetail(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("supplier", supplierService.getById(id)); // entity Supplier
        model.addAttribute("books", productService.getBooksBySupplier(id)); // List<BookCardRes>

        return "customer/supplier";
    }
}
