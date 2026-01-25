package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
