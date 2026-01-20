package com.groupproject.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "publishers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long publisherId;

    @Column(nullable = false, unique = true, columnDefinition = "nvarchar(150)")
    private String name;
}