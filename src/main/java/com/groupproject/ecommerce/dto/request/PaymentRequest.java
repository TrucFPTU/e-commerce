package com.groupproject.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Long orderId;
    private Long amount;
    private String orderInfo;
    private String bankCode;

    public PaymentRequest() {
    }

    public PaymentRequest(Long orderId, Long amount, String orderInfo, String bankCode) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.bankCode = bankCode;
    }
}
