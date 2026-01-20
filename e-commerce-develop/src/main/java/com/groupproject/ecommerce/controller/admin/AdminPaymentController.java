package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.enums.PaymentStatus;
import com.groupproject.ecommerce.service.inter.AdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    @GetMapping
    public String list(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentProvider provider,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        var pageData = adminPaymentService.list(status, provider, page, size);
        model.addAttribute("pageData", pageData);
        model.addAttribute("items", pageData.getContent());

        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("providers", PaymentProvider.values());
        model.addAttribute("status", status);
        model.addAttribute("provider", provider);
        model.addAttribute("size", size);

        return "admin/admin-payments";
    }
}
