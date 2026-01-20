package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.service.inter.AdminPublisherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/publishers")
public class AdminPublisherController {

    private final AdminPublisherService service;

    public AdminPublisherController(AdminPublisherService service) {
        this.service = service;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("items", service.list());
        return "admin/publishers";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long publisherId,
                       @RequestParam String name) {
        service.save(publisherId, name);
        return "redirect:/admin/publishers";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/admin/publishers";
    }
}
