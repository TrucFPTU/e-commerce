package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Category;

import java.util.List;

/**
 * Service interface for Category management
 */
public interface CategoryService {
    
    /**
     * Get all categories
     */
    List<Category> getAllCategories();
    
    /**
     * Get category by ID
     */
    Category getCategoryById(Long id);
    
    /**
     * Save category (create or update)
     */
    Category saveCategory(Long categoryId, String name);
    
    /**
     * Delete category by ID
     */
    void deleteCategory(Long id);
}
