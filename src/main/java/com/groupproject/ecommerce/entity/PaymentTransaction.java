package com.groupproject.ecommerce.entity;

import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_transactions",
        indexes = {
                @Index(name = "idx_payment_txnRef", columnList = "txnRef", unique = true),
                @Index(name = "idx_payment_orderId", columnList = "orderId")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;

    @Column(nullable = false, unique = true, length = 80)
    private String txnRef;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 10)
    private String responseCode;

    @Column(length = 50)
    private String transactionNo;

    @Column(length = 20)
    private String bankCode;

    @Column(length = 20)
    private String cardType;

    private LocalDateTime payDate;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
