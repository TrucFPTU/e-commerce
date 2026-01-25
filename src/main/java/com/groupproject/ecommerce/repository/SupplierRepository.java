package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
