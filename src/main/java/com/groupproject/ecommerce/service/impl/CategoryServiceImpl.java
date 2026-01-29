package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Category;
import com.groupproject.ecommerce.repository.CategoryRepository;
import com.groupproject.ecommerce.service.inter.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Category management
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Category saveCategory(Long categoryId, String name) {
        Category category;
        
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
        } else {
            category = new Category();
        }
        
        category.setName(name);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
