package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Category;

import java.util.List;

public interface AdminCategoryService {
    List<Category> list();
    void save(Long categoryId, String name, Long parentId);
    void softDelete(Long categoryId);
}
