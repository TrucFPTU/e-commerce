package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Supplier;
import com.groupproject.ecommerce.repository.SupplierRepository;
import com.groupproject.ecommerce.service.inter.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));
    }
}
