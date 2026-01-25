package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Product;
import com.groupproject.ecommerce.enums.ProductStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"publisher", "authors"})
    List<Product> findByStatus(ProductStatus status);

    @EntityGraph(attributePaths = {"publisher", "authors", "category"})
    Optional<Product> findByProductIdAndStatus(Long id, ProductStatus status);


    @Query(value = """
        SELECT TOP (8) *
        FROM products p
        WHERE p.status = 'ACTIVE'
          AND p.category_id = :categoryId
          AND p.product_id <> :productId
        ORDER BY NEWID()
        """, nativeQuery = true)
    List<Product> findRandomRelatedByCategory(@Param("categoryId") Long categoryId,
                                              @Param("productId") Long productId);

    @EntityGraph(attributePaths = {"publisher", "supplier", "authors"})
    @Query("""
    select p
    from Product p
    where p.status = com.groupproject.ecommerce.enums.ProductStatus.ACTIVE
      and exists (
        select 1 from p.authors a
        where a.authorId = :authorId
      )
""")
    List<Product> findActiveByAuthorId(@Param("authorId") Long authorId);

    @EntityGraph(attributePaths = {"publisher","supplier","authors"})
    @Query("""
    select p
    from Product p
    where p.status = com.groupproject.ecommerce.enums.ProductStatus.ACTIVE
      and p.publisher.publisherId = :publisherId
""")
    List<Product> findActiveByPublisherId(@Param("publisherId") Long publisherId);

    @EntityGraph(attributePaths = {"publisher","supplier","authors"})
    @Query("""
    select p
    from Product p
    where p.status = com.groupproject.ecommerce.enums.ProductStatus.ACTIVE
      and p.supplier.supplierId = :supplierId
""")
    List<Product> findActiveBySupplierId(@Param("supplierId") Long supplierId);

}
