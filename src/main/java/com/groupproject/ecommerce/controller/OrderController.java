package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.request.CheckoutRequest;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.OrderItem;
import com.groupproject.ecommerce.entity.PaymentTransaction;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.repository.PaymentTransactionRepository;
import com.groupproject.ecommerce.service.inter.OrderItemService;
import com.groupproject.ecommerce.service.inter.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    private static final String SESSION_USER = "LOGIN_USER";

    // =======================
    // DANH SÁCH ĐƠN HÀNG
    // =======================
    @GetMapping
    public String viewOrders(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String status
    ) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders;

        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                orders = orderService.getOrdersByUserAndStatus(user, orderStatus);
            } catch (IllegalArgumentException e) {
                orders = orderService.getOrdersByUser(user);
            }
        } else {
            orders = orderService.getOrdersByUser(user);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("currentStatus", status);
        Map<Long, Boolean> canCancelMap = new HashMap<>();

        for (Order order : orders) {
            boolean canCancel = false;
            if (order.getStatus() == OrderStatus.PROCESSING) {

                List<PaymentTransaction> txs =
                        paymentTransactionRepository.findByOrderOrderByCreatedAtDesc(order);

                // CASH: KHÔNG có payment_transaction
                if (txs == null || txs.isEmpty()) {
                    canCancel = true;
                }
            }

            canCancelMap.put(order.getOrderId(), canCancel);
        }

        model.addAttribute("canCancelMap", canCancelMap);


        return "order/list";
    }

    // =======================
    // CHI TIẾT ĐƠN HÀNG
    // =======================
    @GetMapping("/{orderId}")
    public String viewOrderDetail(
            @PathVariable Long orderId,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId);

        // chặn xem đơn người khác
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/orders";
        }

        // ✅ lấy order items QUA SERVICE
        List<OrderItem> orderItems =
                orderItemService.getItemsByOrderId(orderId);

        // ✅ lấy giao dịch mới nhất (KHÔNG GỌI HÀM MA)
        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findByOrderOrderByCreatedAtDesc(order);

        PaymentTransaction paymentTransaction =
                transactions.isEmpty() ? null : transactions.get(0);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("paymentTransaction", paymentTransaction);

        boolean canCancel = false;

        if (order.getStatus() == OrderStatus.PROCESSING) {

            // CASH không có payment_transaction
            if (paymentTransaction == null) {
                canCancel = true;
            }
        }

        model.addAttribute("canCancel", canCancel);



        return "order/detail";
    }

    @PostMapping("/cancel")
    public String cancelOrder(
            @RequestParam Long orderId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(user, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/orders";
    }



}
