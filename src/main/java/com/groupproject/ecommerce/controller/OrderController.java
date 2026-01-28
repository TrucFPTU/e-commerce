package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.PaymentTransaction;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.repository.PaymentTransactionRepository;
import com.groupproject.ecommerce.service.inter.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private static final String SESSION_USER = "LOGIN_USER";

    @GetMapping
    public String viewOrders(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String status) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders;
        if (status != null && !status.isEmpty()) {
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
        model.addAttribute("user", user);
        model.addAttribute("currentStatus", status);
        model.addAttribute("orderStatuses", OrderStatus.values());

        return "order/list";
    }

    @GetMapping("/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra order có thuộc về user không
            if (order.getUser() == null || !order.getUser().getUserId().equals(user.getUserId())) {
                return "redirect:/orders";
            }

            model.addAttribute("order", order);
            model.addAttribute("user", user);
            
            // Lấy thông tin giao dịch thanh toán nếu có
            List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrder(order);
            if (!transactions.isEmpty()) {
                model.addAttribute("paymentTransaction", transactions.get(0));
            }

            return "order/detail";
        } catch (Exception e) {
            return "redirect:/orders";
        }
    }
}
