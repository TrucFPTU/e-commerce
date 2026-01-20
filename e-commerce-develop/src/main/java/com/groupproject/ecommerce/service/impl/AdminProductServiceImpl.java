package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.*;
import com.groupproject.ecommerce.enums.ProductStatus;
import com.groupproject.ecommerce.repository.*;
import com.groupproject.ecommerce.service.inter.AdminProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final PublisherRepository publisherRepo;
    private final SupplierRepository supplierRepo;
    private final AuthorRepository authorRepo;

    public AdminProductServiceImpl(ProductRepository productRepo,
                                   CategoryRepository categoryRepo,
                                   PublisherRepository publisherRepo,
                                   SupplierRepository supplierRepo,
                                   AuthorRepository authorRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.publisherRepo = publisherRepo;
        this.supplierRepo = supplierRepo;
        this.authorRepo = authorRepo;
    }

    @Override
    public List<Product> list() {
        return productRepo.findAll();
    }

    @Override
    public void save(Long productId, String name, String description, String imageUrl,
                     BigDecimal price, Integer publishYear, Integer stock, ProductStatus status,
                     Long categoryId, Long publisherId, Long supplierId, List<Long> authorIds) {

        Product p = (productId == null)
                ? new Product()
                : productRepo.findById(productId).orElseThrow();

        p.setName(name);
        p.setDescription(description);
        p.setImageUrl(imageUrl);
        p.setPrice(price);
        p.setPublishYear(publishYear);
        p.setStock(stock);
        p.setStatus(status);

        p.setCategory(categoryRepo.findById(categoryId).orElseThrow());
        p.setPublisher(publisherRepo.findById(publisherId).orElseThrow());
        p.setSupplier(supplierRepo.findById(supplierId).orElseThrow());

        if (authorIds == null) authorIds = Collections.emptyList();
        p.setAuthors(authorRepo.findAllById(authorIds));

        productRepo.save(p);
    }

    @Override
    public void setInactive(Long productId) {
        Product p = productRepo.findById(productId).orElseThrow();
        p.setStatus(ProductStatus.INACTIVE); // ✅ đúng nghiệp vụ admin của bạn
        productRepo.save(p);
    }

    @Override public List<Category> categories() { return categoryRepo.findAll(); }
    @Override public List<Publisher> publishers() { return publisherRepo.findAll(); }
    @Override public List<Supplier> suppliers() { return supplierRepo.findAll(); }
    @Override public List<Author> authors() { return authorRepo.findAll(); }
}
