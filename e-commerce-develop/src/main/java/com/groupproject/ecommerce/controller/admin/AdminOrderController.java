package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.service.inter.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        adminOrderService.updateStatus(id, status);
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            Model model
    ) {
        var pageData = adminOrderService.listOrders(status, page, size);

        model.addAttribute("pageData", pageData);
        model.addAttribute("items", pageData.getContent());
        model.addAttribute("statuses", OrderStatus.values());

        model.addAttribute("status", status);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        return "admin/admin-orders";
    }

    @GetMapping("/{orderId}")
    public String detail(@PathVariable Long orderId, Model model) {
        model.addAttribute("order", adminOrderService.getOrderDetail(orderId));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/admin-order-detail";
    }
}
