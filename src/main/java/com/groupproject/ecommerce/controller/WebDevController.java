package com.groupproject.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.groupproject.ecommerce.entity.Entity.*;
import com.groupproject.ecommerce.service.BookService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebDevController {

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    public String root(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/home";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "user", "user@user", "123"));
        users.add(new User(2, "Jane Smith", "jane@example.com", "pass456"));
        users.add(new User(3, "Bob Wilson", "bob@example.com", "bob789"));
        
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                session.setAttribute("loggedInUser", user);
                return "redirect:/home";
            }
        }
        
        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(Model model, HttpSession session,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);

        List<Book> activeBooks = bookService.getActiveBooks();

        model.addAttribute("books", activeBooks);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("totalPages", 0);
        model.addAttribute("currentPage", 0);
        model.addAttribute("hasNext", false);
        model.addAttribute("hasPrevious", false);
        
        return "home";
    }

    @GetMapping("/book/{id}")
    public String bookPage(@PathVariable int id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);
        model.addAttribute("search", "");

        Book book = bookService.getBookById(id);

        if (book == null) {
            return "redirect:/home";
        }

        List<Book> relatedBooks = bookService.getBooks().stream()
            .filter(b -> b.getAuthor().getId() == book.getAuthor().getId() && b.getId() != id)
            .limit(4)
            .collect(Collectors.toList());

        model.addAttribute("book", book);
        model.addAttribute("relatedBooks", relatedBooks);
        return "book";
    }

    @GetMapping("/author/{id}")
    public String authorPage(@PathVariable int id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);
        model.addAttribute("search", "");

        Author author = bookService.getAuthorById(id);

        if (author == null) {
            return "redirect:/home";
        }

        List<Book> authorBooks = bookService.getBooks().stream()
            .filter(b -> b.getAuthor().getId() == id && "active".equals(b.getStatus()))
            .collect(Collectors.toList());

        model.addAttribute("author", author);
        model.addAttribute("books", authorBooks);
        return "author";
    }

    @GetMapping("/publisher/{id}")
    public String publisherPage(@PathVariable int id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);
        model.addAttribute("search", "");

        Publisher publisher = bookService.getPublisherById(id);

        if (publisher == null) {
            return "redirect:/home";
        }

        List<Book> publisherBooks = bookService.getBooks().stream()
            .filter(b -> b.getPublisher().getId() == id && "active".equals(b.getStatus()))
            .collect(Collectors.toList());

        model.addAttribute("publisher", publisher);
        model.addAttribute("books", publisherBooks);
        return "publisher";
    }

    @GetMapping("/supplier/{id}")
    public String supplierPage(@PathVariable int id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);
        model.addAttribute("search", "");

        Supplier supplier = bookService.getSupplierById(id);

        if (supplier == null) {
            return "redirect:/home";
        }

        List<Book> supplierBooks = bookService.getBooks().stream()
            .filter(b -> b.getSupplier().getId() == id && "active".equals(b.getStatus()))
            .collect(Collectors.toList());

        model.addAttribute("supplier", supplier);
        model.addAttribute("books", supplierBooks);
        return "supplier";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false, defaultValue = "") String fullname, 
                                @RequestParam(required = false, defaultValue = "") String email, 
                                @RequestParam(required = false, defaultValue = "") String password, 
                                HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        String newFullname = fullname.isEmpty() ? currentUser.getFullname() : fullname;
        String newEmail = email.isEmpty() ? currentUser.getEmail() : email;
        String newPassword = password.isEmpty() ? currentUser.getPassword() : password;
        User updatedUser = new User(currentUser.getId(), newFullname, newEmail, newPassword);
        session.setAttribute("loggedInUser", updatedUser);
        return "redirect:/profile";
    }
}
