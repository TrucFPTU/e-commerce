package com.groupproject.ecommerce.entity;

import com.groupproject.ecommerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false, unique = true, length = 40)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime placedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private java.util.List<OrderItem> items;


    @PrePersist
    void prePersist() {
        if (placedAt == null) placedAt = LocalDateTime.now();
    }
}