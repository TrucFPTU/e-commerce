package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Supplier;
import com.groupproject.ecommerce.repository.SupplierRepository;
import com.groupproject.ecommerce.service.inter.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Supplier management
 */
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public Supplier getById(Long id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Supplier saveSupplier(Long supplierId, String name) {
        Supplier supplier;
        
        if (supplierId != null) {
            supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        } else {
            supplier = new Supplier();
        }
        
        supplier.setName(name);
        return supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}
