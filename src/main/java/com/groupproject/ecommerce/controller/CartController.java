package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.request.AddToCartRequest;
import com.groupproject.ecommerce.dto.request.CheckoutRequest;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    private static final String SESSION_USER = "LOGIN_USER";

    /* ================= ADD TO CART ================= */

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
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm vào giỏ hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }

    /* ================= VIEW CART ================= */

    @GetMapping
    public String viewCart(HttpSession session, Model model) {

        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartService.getCartItems(user);

        BigDecimal totalAmount = cartItems.stream()
                .map(i -> i.getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);

        return "cart/view";
    }

    /* ================= UPDATE QUANTITY ================= */

    @PostMapping("/update/{id}")
    public String updateQuantity(@PathVariable Long id,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) return "redirect:/login";

        try {
            cartService.updateQuantity(id, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /* ================= REMOVE ITEM ================= */

    @PostMapping("/remove/{id}")
    public String removeCartItem(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) return "redirect:/login";

        try {
            cartService.removeCartItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /* ================= CHECKOUT ================= */

    @PostMapping("/checkout")
    public String checkout(@RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam String paymentMethod,
                           @RequestParam String selectedItems,
                           HttpSession session,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) return "redirect:/login";

        try {
            if (selectedItems == null || selectedItems.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa chọn sản phẩm");
                return "redirect:/cart";
            }

            List<Long> selectedCartItemIds = new ArrayList<>();
            for (String id : selectedItems.split(",")) {
                try {
                    selectedCartItemIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException ignored) {}
            }

            if (selectedCartItemIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Danh sách sản phẩm không hợp lệ");
                return "redirect:/cart";
            }

            CheckoutRequest checkoutRequest = new CheckoutRequest();
            checkoutRequest.setPhone(phone);
            checkoutRequest.setAddress(address);
            checkoutRequest.setPaymentMethod(paymentMethod);

            // ✅ GỌI 1 LUỒNG DUY NHẤT
            Order order = orderService.checkout(user, checkoutRequest, selectedCartItemIds);

            if ("CASH".equalsIgnoreCase(paymentMethod)) {
                return "redirect:/payment/result"
                        + "?status=success"
                        + "&paymentMethod=CASH"
                        + "&orderCode=" + order.getOrderCode()
                        + "&amount=" + order.getTotal().longValue(); // ✅ FIX
            }


            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setOrderId(order.getOrderId());
                paymentRequest.setAmount(order.getTotal().longValue());
                paymentRequest.setOrderInfo("Thanh toan don hang " + order.getOrderCode());

                String ipAddress = getIpAddress(request);
                PaymentResponse response =
                        paymentService.createVNPayPayment(paymentRequest, ipAddress);

                return "redirect:" + response.getPaymentUrl();
            }

            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ");
            return "redirect:/cart";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ip;
    }
}
