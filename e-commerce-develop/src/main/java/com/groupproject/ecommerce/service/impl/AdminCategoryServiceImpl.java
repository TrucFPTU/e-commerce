package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Category;
import com.groupproject.ecommerce.repository.CategoryRepository;
import com.groupproject.ecommerce.service.inter.AdminCategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository repo;

    public AdminCategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }


    @Override
    public List<Category> list() {
        return repo.findByDeletedAtIsNullOrderByCategoryIdDesc();
    }

    @Override
    public void save(Long categoryId, String name, Long parentId) {
        Category c = (categoryId == null) ? new Category() : repo.findById(categoryId).orElseThrow();

        if(categoryId == null && repo.existsByNameAndDeletedAtIsNull(name)){
            throw new RuntimeException("Category name already exists");
        }

        c.setName(name);

        if(parentId != null){
            if(categoryId != null && parentId.equals(categoryId)){
                throw new RuntimeException("Parent cannot be itself");
            }
            Category p = repo.findById(parentId).orElse(null);
            c.setParent(p);
        } else {
            c.setParent(null);
        }

        if(c.getCreatedAt() == null) c.setCreatedAt(java.time.LocalDateTime.now());
        c.setUpdatedAt(java.time.LocalDateTime.now());

        repo.save(c);
    }

    @Override
    public void softDelete(Long categoryId) {
        Category c = repo.findById(categoryId).orElseThrow();
        c.setDeletedAt(LocalDateTime.now());
        repo.save(c);
    }
}
