package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author,Long> {
    List<Author> findByDeletedAtIsNullOrderByAuthorIdDesc();
}
