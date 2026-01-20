package com.groupproject.ecommerce.dto.response;

import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminPaymentTransactionListItemResponse {
    private Long paymentId;
    private Long orderId;

    private PaymentProvider provider;
    private String txnRef;

    private BigDecimal amount;
    private String currency;

    private PaymentStatus status;

    private String responseCode;
    private String transactionNo;

    private LocalDateTime createdAt;
    private LocalDateTime payDate;
}
