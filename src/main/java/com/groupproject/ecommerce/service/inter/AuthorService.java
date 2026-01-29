package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Author;

import java.util.List;

/**
 * Service interface for Author management
 */
public interface AuthorService {
    
    /**
     * Get all authors
     */
    List<Author> getAllAuthors();
    
    /**
     * Get author by ID
     */
    Author getById(Long id);
    
    /**
     * Save author (create or update)
     */
    Author saveAuthor(Long authorId, String name);
    
    /**
     * Delete author by ID
     */
    void deleteAuthor(Long id);
}
