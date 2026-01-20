package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Author;

import java.util.List;

public interface AdminAuthorService {
    List<Author> list();
    void save(Long authorId, String name);
    void softDelete(Long authorId);
}
