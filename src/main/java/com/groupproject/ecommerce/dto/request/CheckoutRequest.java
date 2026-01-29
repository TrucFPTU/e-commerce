package com.groupproject.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private String phone;
    private String address;
    private String paymentMethod; // COD | VNPAY
}
