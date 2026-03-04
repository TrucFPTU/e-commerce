package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.request.PaymentRequest;
import com.groupproject.ecommerce.dto.response.PaymentResponse;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.enums.PaymentStatus;
import com.groupproject.ecommerce.service.inter.OrderService;
import com.groupproject.ecommerce.service.inter.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping("/vnpay/retry")
    public String retryPayment(
            @RequestParam Long orderId,
            HttpSession session,
            HttpServletRequest request
    ) {

        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) return "redirect:/login";

        Order order = orderService.getOrderById(orderId);

        // chặn thanh toán đơn người khác
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/orders";
        }

        // chỉ cho retry khi AWAITING_PAYMENT hoặc CANCELLED
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT &&
                order.getStatus() != OrderStatus.CANCELLED) {
            return "redirect:/orders/" + orderId;
        }

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getOrderId());
        paymentRequest.setAmount(order.getTotal().longValue());
        paymentRequest.setOrderInfo("Thanh toan lai don hang " + order.getOrderCode());

        String ipAddress = request.getRemoteAddr();
        PaymentResponse response =
                paymentService.createVNPayPayment(paymentRequest, ipAddress);

        return "redirect:" + response.getPaymentUrl();
    }



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


    @GetMapping("/callback")
    public String handleVNPayCallback(@RequestParam Map<String, String> params, Model model) {

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        log.info("[VNPAY CALLBACK] txnRef={}, responseCode={}, transactionStatus={}",
                txnRef, responseCode, transactionStatus);

        boolean isValid = paymentService.handleVNPayCallback(params);
        log.info("[VNPAY CALLBACK] signatureValid={}", isValid);

        if (!isValid) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Chữ ký không hợp lệ. Giao dịch bị từ chối");
            addCommonAttributes(params, model);
            return "payment/result";
        }

        // ✅ PaymentServiceImpl đã tự update PaymentTransaction + Order.status rồi
        processValidCallback(params, model);
        addCommonAttributes(params, model);
        return "payment/result";
    }





    /**
     * Endpoint hiển thị kết quả thanh toán COD
     * @param model Model chứa attributes từ redirect
     * @return View result page
     */
    @GetMapping("/result")
    public String showPaymentResult(
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) BigDecimal amount,
            Model model
    ) {

        // ===== CASH / COD =====
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            model.addAttribute("status", "success");
            model.addAttribute("message", "Đặt hàng thành công. Thanh toán khi nhận hàng.");
            model.addAttribute("paymentMethod", "CASH");
            model.addAttribute("orderCode", orderCode);
            model.addAttribute("amount", amount);
            return "payment/result";
        }

        // ===== FALLBACK (phòng trường hợp truy cập thẳng URL) =====
        model.addAttribute("status", "error");
        model.addAttribute("message", "Không xác định được phương thức thanh toán");
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
