package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.*;
import com.groupproject.ecommerce.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public interface AdminProductService {
    List<Product> list();

    void save(Long productId,
              String name,
              String description,
              String imageUrl,
              BigDecimal price,
              Integer publishYear,
              Integer stock,
              ProductStatus status,
              Long categoryId,
              Long publisherId,
              Long supplierId,
              List<Long> authorIds);

    void setInactive(Long productId);

    List<Category> categories();
    List<Publisher> publishers();
    List<Supplier> suppliers();
    List<Author> authors();
}
