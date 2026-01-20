package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Supplier;
import com.groupproject.ecommerce.repository.SupplierRepository;
import com.groupproject.ecommerce.service.inter.AdminSupplierService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AdminSupplierServiceImpl implements AdminSupplierService {

    private final SupplierRepository repo;
    public AdminSupplierServiceImpl(SupplierRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Supplier> list() {
        return repo.findAll();
    }

    @Override
    public void save(Long supplierId, String name) {
        Supplier s = (supplierId == null) ? new Supplier() : repo.findById(supplierId).orElseThrow();

        s.setName(name);
        repo.save(s);
    }

    @Override
    public void delete(Long supplierId) {
        repo.deleteById(supplierId);
    }
}
