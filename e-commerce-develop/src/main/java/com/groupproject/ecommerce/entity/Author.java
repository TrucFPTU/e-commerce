package com.groupproject.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "authors")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorId;

    @Column(nullable = false, columnDefinition = "nvarchar(150)")
    private String name;

    private java.time.LocalDateTime deletedAt;
}