package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Supplier;

import java.util.List;

public interface AdminSupplierService {
    List<Supplier> list();
    void save(Long supplierId, String name);
    void delete(Long supplierId);
}
