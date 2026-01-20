package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    List<Category> findByDeletedAtIsNullOrderByCategoryIdDesc();
    boolean existsByNameAndDeletedAtIsNull(String name);
}
