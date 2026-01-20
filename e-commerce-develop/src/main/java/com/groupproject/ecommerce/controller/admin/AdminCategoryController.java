package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.dto.request.CategoryRequest;
import com.groupproject.ecommerce.service.inter.AdminCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    @Autowired
    private AdminCategoryService service;

    public AdminCategoryController(AdminCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public String page(Model model){
        model.addAttribute("items", service.list());
        model.addAttribute("form", new CategoryRequest());
        return "admin/categories";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("form") CategoryRequest form){
        service.save(form.getCategoryId(), form.getName(), form.getParentId());
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id){
        service.softDelete(id);
        return "redirect:/admin/categories";
    }
}
