package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.request.AddToCartRequest;
import com.groupproject.ecommerce.dto.request.PaymentRequest;
import com.groupproject.ecommerce.dto.response.PaymentResponse;
import com.groupproject.ecommerce.entity.CartItem;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.service.inter.CartService;
import com.groupproject.ecommerce.service.inter.OrderService;
import com.groupproject.ecommerce.service.inter.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private static final String SESSION_USER = "LOGIN_USER";

    @PostMapping("/add")
    public String addToCart(@Valid @ModelAttribute AddToCartRequest request,
                            BindingResult bindingResult,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            @RequestHeader(value = "Referer", required = false) String referer) {

        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) return "redirect:/login";

        String redirectUrl = (referer != null && !referer.isBlank()) ? referer : "/homepage";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ");
            return "redirect:" + redirectUrl;
        }

        try {
            cartService.addToCart(user, request.getProductId(), request.getQuantity());
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm vào giỏ hàng ✅");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }


    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems(user);
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", user);

        return "cart/view";
    }

    @PostMapping("/update/{cartItemId}")
    public String updateQuantity(@PathVariable Long cartItemId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            cartService.updateQuantity(cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giỏ hàng thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeItem(@PathVariable Long cartItemId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeCartItem(cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        cartService.clearCart(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ giỏ hàng");

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam String phone,
                          @RequestParam String address,
                          @RequestParam String paymentMethod,
                          HttpSession session,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Lấy giỏ hàng
            List<CartItem> cartItems = cartService.getCartItems(user);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống");
                return "redirect:/cart";
            }

            // Tính tổng tiền
            BigDecimal totalAmount = cartItems.stream()
                    .map(item -> item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tạo đơn hàng với status PROCESSING
            Order order = orderService.createOrderFromCart(user, cartItems, phone, address, totalAmount);

            // Xóa giỏ hàng sau khi tạo đơn thành công
            cartService.clearCart(user);

            // Xử lý theo phương thức thanh toán
            if ("CASH".equalsIgnoreCase(paymentMethod)) {
                // Thanh toán tiền mặt - chuyển đến trang thành công
                redirectAttributes.addFlashAttribute("status", "success");
                redirectAttributes.addFlashAttribute("message", "Đặt hàng thành công! Bạn sẽ thanh toán khi nhận hàng.");
                redirectAttributes.addFlashAttribute("orderCode", order.getOrderCode());
                redirectAttributes.addFlashAttribute("amount", totalAmount.longValue());
                redirectAttributes.addFlashAttribute("paymentMethod", "COD");
                return "redirect:/payment/result";
            } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                // Thanh toán VNPay - tạo payment URL
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setOrderId(order.getOrderId());
                paymentRequest.setAmount(totalAmount.longValue());
                paymentRequest.setOrderInfo("Thanh toan don hang " + order.getOrderCode());

                // Lấy IP address
                String ipAddress = getIpAddress(request);

                // Tạo VNPay payment URL
                PaymentResponse paymentResponse = paymentService.createVNPayPayment(paymentRequest, ipAddress);

                if ("success".equals(paymentResponse.getStatus())) {
                    // Redirect đến VNPay
                    return "redirect:" + paymentResponse.getPaymentUrl();
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo thanh toán: " + paymentResponse.getMessage());
                    return "redirect:/cart";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ");
                return "redirect:/cart";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}