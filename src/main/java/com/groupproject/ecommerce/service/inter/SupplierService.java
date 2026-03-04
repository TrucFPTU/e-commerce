package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Supplier;

import java.util.List;

/**
 * Service interface for Supplier management
 */
public interface SupplierService {
    
    /**
     * Get all suppliers
     */
    List<Supplier> getAllSuppliers();
    
    /**
     * Get supplier by ID
     */
    Supplier getById(Long id);
    
    /**
     * Save supplier (create or update)
     */
    Supplier saveSupplier(Long supplierId, String name);
    
    /**
     * Delete supplier by ID
     */
    void deleteSupplier(Long id);
}
