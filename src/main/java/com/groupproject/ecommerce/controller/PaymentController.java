package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.request.PaymentRequest;
import com.groupproject.ecommerce.dto.response.PaymentResponse;
import com.groupproject.ecommerce.service.inter.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý các endpoint liên quan đến thanh toán VNPay
 */
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private static final String SUCCESS_RESPONSE_CODE = "00";
    private static final int VNPAY_AMOUNT_DIVIDER = 100;
    
    private final PaymentService paymentService;

    /**
     * API endpoint tạo URL thanh toán VNPay
     * @param request Payment request
     * @param httpRequest HTTP request để lấy IP
     * @return ResponseEntity chứa payment URL
     */
    @PostMapping("/create-vnpay")
    @ResponseBody
    public ResponseEntity<PaymentResponse> createVNPayPayment(
            @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = extractIpAddress(httpRequest);
        PaymentResponse response = paymentService.createVNPayPayment(request, ipAddress);
        
        log.info("Create VNPay payment - OrderId: {}, Status: {}", request.getOrderId(), response.getStatus());
        
        return "success".equals(response.getStatus()) 
                ? ResponseEntity.ok(response) 
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Callback endpoint từ VNPay sau khi thanh toán
     * @param params Parameters từ VNPay
     * @param model Model để truyền dữ liệu sang view
     * @return View result page
     */
    @GetMapping("/callback")
    public String handleVNPayCallback(@RequestParam Map<String, String> params, Model model) {
        log.info("Received VNPay callback for transaction: {}", params.get("vnp_TxnRef"));
        
        boolean isValid = paymentService.handleVNPayCallback(params);
        
        if (isValid) {
            processValidCallback(params, model);
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Chữ ký không hợp lệ. Giao dịch bị từ chối");
        }
        
        addCommonAttributes(params, model);
        return "payment/result";
    }

    /**
     * Endpoint hiển thị kết quả thanh toán COD
     * @param model Model chứa attributes từ redirect
     * @return View result page
     */
    @GetMapping("/result")
    public String showPaymentResult(Model model) {
        return "payment/result";
    }

    /**
     * API endpoint lấy trạng thái thanh toán của đơn hàng
     * @param orderId ID đơn hàng
     * @return ResponseEntity chứa payment status
     */
    @GetMapping("/status/{orderId}")
    @ResponseBody
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long orderId) {
        log.info("Getting payment status for order: {}", orderId);
        PaymentResponse response = paymentService.getPaymentStatus(orderId);
        return ResponseEntity.ok(response);
    }

    // ==================== Private Helper Methods ====================

    private void processValidCallback(Map<String, String> params, Model model) {
        String responseCode = params.get("vnp_ResponseCode");
        
        if (SUCCESS_RESPONSE_CODE.equals(responseCode)) {
            model.addAttribute("status", "success");
            model.addAttribute("message", "Giao dịch của bạn đã được xử lý thành công");
        } else {
            model.addAttribute("status", "failed");
            model.addAttribute("message", "Giao dịch không thành công. Vui lòng thử lại");
        }
        
        model.addAttribute("bankCode", params.get("vnp_BankCode"));
        model.addAttribute("transactionNo", params.get("vnp_TransactionNo"));
    }

    private void addCommonAttributes(Map<String, String> params, Model model) {
        model.addAttribute("txnRef", params.get("vnp_TxnRef"));
        
        String amountStr = params.get("vnp_Amount");
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                long amount = Long.parseLong(amountStr) / VNPAY_AMOUNT_DIVIDER;
                model.addAttribute("amount", amount);
            } catch (NumberFormatException e) {
                log.error("Invalid amount format: {}", amountStr, e);
            }
        }
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        
        if (isNullOrEmpty(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        
        if (isNullOrEmpty(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        return ipAddress;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
