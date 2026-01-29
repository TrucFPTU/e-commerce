package com.groupproject.ecommerce.enums;

public enum OrderStatus {
    AWAITING_PAYMENT, // chờ thanh toán
    PROCESSING,
    SHIPPING,
    COMPLETED,
    CANCELLED,
    RETURNED
}