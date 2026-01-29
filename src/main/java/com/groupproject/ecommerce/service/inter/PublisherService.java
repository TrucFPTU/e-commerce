package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Publisher;

import java.util.List;

/**
 * Service interface for Publisher management
 */
public interface PublisherService {
    
    /**
     * Get all publishers
     */
    List<Publisher> getAllPublishers();
    
    /**
     * Get publisher by ID
     */
    Publisher getById(Long id);
    
    /**
     * Save publisher (create or update)
     */
    Publisher savePublisher(Long publisherId, String name);
    
    /**
     * Delete publisher by ID
     */
    void deletePublisher(Long id);
}
