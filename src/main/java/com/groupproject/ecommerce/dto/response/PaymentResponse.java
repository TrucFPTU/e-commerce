package com.groupproject.ecommerce.dto.response;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class PaymentResponse {
    private String status;
    private String message;
    private String paymentUrl;

    public PaymentResponse() {
    }

    public PaymentResponse(String status, String message, String paymentUrl) {
        this.status = status;
        this.message = message;
        this.paymentUrl = paymentUrl;
    }
}
