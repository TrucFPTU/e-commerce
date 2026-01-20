package com.groupproject.ecommerce.controller.admin;

import com.groupproject.ecommerce.dto.response.PaymentCallbackLogView;
import com.groupproject.ecommerce.entity.PaymentCallbackLog;
import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.service.inter.PaymentCallbackLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/payment-callback-logs")
@RequiredArgsConstructor
public class AdminPaymentCallbackLogController {

    private final PaymentCallbackLogService logService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String txnRef,
            Pageable pageable,
            Model model
    ) {
        var pageData = logService.getLogs(provider, txnRef, pageable);

        var views = pageData.getContent().stream()
                .map(this::toView)
                .toList();

        model.addAttribute("items", views);
        model.addAttribute("pageData", pageData);
        model.addAttribute("provider", provider);
        model.addAttribute("txnRef", txnRef);

        return "admin/admin-payment-callback-logs";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PaymentCallbackLog e = logService.getById(id);

        model.addAttribute("item", toView(e));
        return "admin/admin-payment-callback-log-detail";
    }

    // ===== Mapper: Entity -> View DTO =====
    private PaymentCallbackLogView toView(PaymentCallbackLog e) {
        return PaymentCallbackLogView.builder()
                .logId(e.getLogId())
                .provider(toProviderEnum(e.getProvider()))
                .txnRef(e.getTxnRef())
                .receivedAt(e.getReceivedAt())
                .validSignature(e.isValidSignature())
                .clientIp(e.getClientIp())
                .rawPayload(e.getRawPayload())
                // hiện tại entity chưa có httpStatus nên để null
                .httpStatus(null)
                .build();
    }

    // ===== Convert String -> Enum =====
    private PaymentProvider toProviderEnum(String providerValue) {
        if (providerValue == null || providerValue.isBlank()) return null;

        try {
            return PaymentProvider.valueOf(providerValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null; // hoặc throw nếu muốn fail-fast
        }
    }
}
