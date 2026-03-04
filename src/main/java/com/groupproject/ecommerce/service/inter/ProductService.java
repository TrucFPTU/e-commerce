package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.response.BookCardRes;
import com.groupproject.ecommerce.entity.Product;
import com.groupproject.ecommerce.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Product management
 */
public interface ProductService {

    // Customer-facing methods
    List<BookCardRes> getHomeBooks();
    BookCardRes getBookCardById(Long id);
    List<BookCardRes> getRelatedBooks(Long productId);
    List<BookCardRes> getBooksByAuthor(Long authorId);
    List<BookCardRes> getBooksByPublisher(Long publisherId);
    List<BookCardRes> getBooksBySupplier(Long supplierId);
    List<BookCardRes> searchHomeBooks(String keyword);
    String getProductsForPrompt(int limit);

    // Admin CRUD methods
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product saveProduct(Long productId, String name, String description, String imageUrl,
                        BigDecimal price, Integer publishYear, Integer stock, ProductStatus status,
                        Long categoryId, Long publisherId, Long supplierId, List<Long> authorIds);
    void deleteProduct(Long id);
}