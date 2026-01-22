package com.groupproject.ecommerce.entity;

import com.groupproject.ecommerce.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "conversations",
        indexes = {
                @Index(name = "idx_conv_customer_status", columnList = "customer_id,status"),
                @Index(name = "idx_conv_staff_status", columnList = "staff_id,status")
        }
)
@Getter
@Setter
public class Conversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ConversationStatus status; // OPEN, CLOSED

    @Column(nullable = false)
    private java.time.Instant createdAt;

    private java.time.Instant lastMessageAt;
}
