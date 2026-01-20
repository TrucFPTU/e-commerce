package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.service.inter.AdminAuthorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/authors")
public class AdminAuthorController {

    private final AdminAuthorService service;

    public AdminAuthorController(AdminAuthorService service) {
        this.service = service;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("items", service.list());
        return "admin/authors";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long authorId,
                       @RequestParam String name) {
        service.save(authorId, name);
        return "redirect:/admin/authors";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.softDelete(id);
        return "redirect:/admin/authors";
    }
}
