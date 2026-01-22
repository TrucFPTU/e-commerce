package com.groupproject.ecommerce.entity;

import com.groupproject.ecommerce.enums.MessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "messages",
        indexes = {
                @Index(name = "idx_msg_conv_created", columnList = "conversation_id,created_at")
        }
)
@Getter
@Setter
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="conversation_id", nullable=false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="sender_id", nullable=false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private MessageType type; // TEXT, IMAGE, FILE

    @Column(columnDefinition = "NVARCHAR(2000)")
    private String content; // text hoặc caption

    @Column(nullable=false)
    private java.time.Instant createdAt;

    private java.time.Instant readAt; // optional
}
