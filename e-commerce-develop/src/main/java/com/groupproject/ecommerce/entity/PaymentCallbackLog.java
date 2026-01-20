package com.groupproject.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_callback_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String provider;        // VNPAY / MOMO / PAYPAL
    private String txnRef;          // tham chiếu giao dịch
    private String rawPayload;      // JSON nhận từ gateway
    private String clientIp;        // IP callback
    private LocalDateTime receivedAt;

    private boolean validSignature; // có hợp lệ không
    private Integer httpStatus;

}
