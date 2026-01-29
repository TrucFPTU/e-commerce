
package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.dto.response.BookCardRes;
import com.groupproject.ecommerce.entity.Author;
import com.groupproject.ecommerce.entity.Category;
import com.groupproject.ecommerce.entity.Product;
import com.groupproject.ecommerce.entity.Publisher;
import com.groupproject.ecommerce.entity.Supplier;
import com.groupproject.ecommerce.enums.ProductStatus;
import com.groupproject.ecommerce.repository.AuthorRepository;
import com.groupproject.ecommerce.repository.CategoryRepository;
import com.groupproject.ecommerce.repository.ProductRepository;
import com.groupproject.ecommerce.repository.PublisherRepository;
import com.groupproject.ecommerce.repository.SupplierRepository;
import com.groupproject.ecommerce.service.inter.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final SupplierRepository supplierRepository;
    private final AuthorRepository authorRepository;

    @Override
    public String getProductsForPrompt(int limit) {
        List<BookCardRes> list = getHomeBooks();
        if (list == null || list.isEmpty()) return "\n\n(Hiện chưa có sản phẩm để gợi ý.)";

        int n = Math.min(limit, list.size());
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nAvailable books (for recommendation):\n");
        for (int i = 0; i < n; i++) {
            BookCardRes b = list.get(i);
            sb.append("- ")
                    .append(b.getName())
                    .append(" | Author: ").append(b.getAuthorName())
                    .append(" | Publisher: ").append(b.getPublisherName())
                    .append(" | Year: ").append(b.getPublishYear())
                    .append(" | Price: ").append(b.getPrice())
                    .append(" | Stock: ").append(b.getStock())
                    .append("\n");
        }
        sb.append("Rules: Recommend only from this list if possible.\n");
        return sb.toString();
    }



    @Override
    public List<BookCardRes> searchHomeBooks(String keyword) {
        return productRepository.searchActive(keyword)
                .stream()
                .map(this::toBookCardRes)
                .toList();
    }

    @Override
    public List<BookCardRes> getBooksBySupplier(Long supplierId) {
        return productRepository.findActiveBySupplierId(supplierId)
                .stream()
                .map(this::toBookCardRes)
                .toList();
    }


    @Override
    public List<BookCardRes> getBooksByAuthor(Long authorId) {
        return productRepository.findActiveByAuthorId(authorId)
                .stream()
                .map(this::toBookCardRes)
                .toList();
    }

    @Override
    public List<BookCardRes> getBooksByPublisher(Long publisherId) {
        return productRepository.findActiveByPublisherId(publisherId)
                .stream()
                .map(this::toBookCardRes)
                .toList();
    }


    @Override
    public List<BookCardRes> getHomeBooks() {
        return productRepository.findByStatus(ProductStatus.ACTIVE)
                .stream()
                .map(this::toBookCardRes)
                .toList();
    }

    @Override
    public BookCardRes getBookCardById(Long id) {
        Product p = productRepository.findByProductIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Book not found: " + id));
        return toBookCardRes(p);
    }

    private BookCardRes toBookCardRes(Product p) {
        Long authorId = (p.getAuthors() != null && !p.getAuthors().isEmpty())
                ? p.getAuthors().get(0).getAuthorId()
                : null;

        String authorName = (p.getAuthors() != null && !p.getAuthors().isEmpty())
                ? p.getAuthors().get(0).getName()
                : "Unknown";

        Long publisherId = (p.getPublisher() != null) ? p.getPublisher().getPublisherId() : null;
        String publisherName = (p.getPublisher() != null) ? p.getPublisher().getName() : "Unknown";

        Long supplierId = (p.getSupplier() != null) ? p.getSupplier().getSupplierId() : null;
        String supplierName = (p.getSupplier() != null) ? p.getSupplier().getName() : "Unknown";
        Long categoryId = (p.getCategory() != null) ? p.getCategory().getCategoryId() : null;
        String categoryName = (p.getCategory() != null) ? p.getCategory().getName() : "Unknown";

        return BookCardRes.builder()
                .id(p.getProductId())
                .name(p.getName())
                .imageUrl(p.getImageUrl())
                .price(p.getPrice())
                .publishYear(p.getPublishYear())
                .stock(p.getStock())
                .authorId(authorId)
                .authorName(authorName)
                .publisherId(publisherId)
                .publisherName(publisherName)
                .supplierId(supplierId)
                .supplierName(supplierName)
                .description(p.getDescription())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();
    }




    @Override
    public List<BookCardRes> getRelatedBooks(Long productId) {
        Product current = productRepository.findByProductIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Book not found: " + productId));

        Long categoryId = current.getCategory().getCategoryId();

        return productRepository.findRandomRelatedByCategory(categoryId, productId)
                .stream()
                .map(this::toBookCardRes)
                .toList();
    }

    // ==================== ADMIN CRUD METHODS ====================

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Product saveProduct(Long productId, String name, String description, String imageUrl,
                              BigDecimal price, Integer publishYear, Integer stock, ProductStatus status,
                              Long categoryId, Long publisherId, Long supplierId, List<Long> authorIds) {
        Product product;
        
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        } else {
            product = new Product();
        }
        
        // Update basic fields
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setPrice(price);
        product.setPublishYear(publishYear);
        product.setStock(stock);
        product.setStatus(status);
        
        // Set relationships
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
        product.setCategory(category);
        
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà xuất bản"));
        product.setPublisher(publisher);
        
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        product.setSupplier(supplier);
        
        List<Author> authors = authorRepository.findAllById(authorIds);
        product.setAuthors(authors);
        
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        productRepository.delete(product);
    }



}
