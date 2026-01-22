package com.groupproject.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="attachments")
@Getter
@Setter
public class Attachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="message_id", nullable=false, unique=true)
    private Message message;

    @Column(nullable=false, length=255)
    private String originalName;

    @Column(nullable=false, length=255)
    private String storedName;

    @Column(nullable=false, length=100)
    private String mimeType;

    @Column(nullable=false)
    private Long size;

    @Column(nullable=false, length=500)
    private String url; // /chat/file/{id} hoặc /uploads/...
}

