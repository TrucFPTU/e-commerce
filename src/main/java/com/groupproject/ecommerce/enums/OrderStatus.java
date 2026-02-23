package com.groupproject.ecommerce.enums;

public enum OrderStatus {
    AWAITING_PAYMENT, // chờ thanh toán
    PROCESSING,
    SHIPPING,
    SHIPPED,
    COMPLETED,
    CANCELLED,
    RETURNED,
    CONFIRMED,
    ISSUE,
    ISSUE_RECEIVED
}