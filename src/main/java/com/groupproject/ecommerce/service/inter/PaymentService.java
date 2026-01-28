package com.groupproject.ecommerce.service.inter;

import java.util.Map;

import com.groupproject.ecommerce.dto.request.PaymentRequest;
import com.groupproject.ecommerce.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createVNPayPayment(PaymentRequest request, String ipAddress);
    
    boolean handleVNPayCallback(Map<String, String> params);
    
    PaymentResponse getPaymentStatus(Long orderId);
}
