package com.groupproject.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suppliers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;

    @Column(nullable = false, unique = true, columnDefinition = "nvarchar(150)")
    private String name;
}