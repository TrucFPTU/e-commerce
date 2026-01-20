package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.enums.ProductStatus;
import com.groupproject.ecommerce.service.inter.AdminProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final AdminProductService service;

    public AdminProductController(AdminProductService service) {
        this.service = service;
    }

    @GetMapping
    public String page(Model model) {
        var items = service.list();

        // ✅ build map: productId -> "1,2,3"
        Map<Long, String> productAuthorIds = new HashMap<>();
        for (var p : items) {
            String ids = (p.getAuthors() == null) ? "" :
                    p.getAuthors().stream()
                            .map(a -> String.valueOf(a.getAuthorId()))
                            .collect(Collectors.joining(","));
            productAuthorIds.put(p.getProductId(), ids);
        }

        model.addAttribute("items", items);
        model.addAttribute("productAuthorIds", productAuthorIds);

        model.addAttribute("categories", service.categories());
        model.addAttribute("publishers", service.publishers());
        model.addAttribute("suppliers", service.suppliers());
        model.addAttribute("authors", service.authors());
        model.addAttribute("statuses", ProductStatus.values());
        return "admin/products";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long productId,
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
                       @RequestParam(required = false) List<Long> authorIds) {

        service.save(productId, name, description, imageUrl, price, publishYear, stock, status,
                categoryId, publisherId, supplierId, authorIds);

        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.setInactive(id);
        return "redirect:/admin/products";
    }
}
