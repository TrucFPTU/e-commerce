package com.groupproject.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, unique = true, columnDefinition = "nvarchar(120)")
    private String name;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Category parent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
