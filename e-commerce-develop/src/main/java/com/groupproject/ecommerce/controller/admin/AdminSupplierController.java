package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.service.inter.AdminSupplierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/suppliers")
public class AdminSupplierController {

    private final AdminSupplierService service;

    public AdminSupplierController(AdminSupplierService service) {
        this.service = service;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("items", service.list());
        return "admin/suppliers";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long supplierId,
                       @RequestParam String name) {
        service.save(supplierId, name);
        return "redirect:/admin/suppliers";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/admin/suppliers";
    }
}
